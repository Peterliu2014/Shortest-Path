/**
 * 实现代码文件
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;



import java.util.*;

public final class Route {
    /**
     * 你需要完成功能的入口
     *
     * @author XXX
     * @version V1
     * @since 2016-3-4
     */

    private static int permutations=0;
    private static final ArrayList<ArrayList<Integer>> permutaion=new ArrayList<ArrayList<Integer>>();
    private static  Integer[] demandRouterArray;
    private static double[][] distance;
    private static int[] sortedArrayRouterID;
    private static final Set<Integer> routerID = new HashSet<Integer>();//路由节点ID的集合
    public static final Map<Integer, ArrayList<Integer>> neighbourTable = new HashMap<Integer,ArrayList<Integer>>();
    static Map<String, String> edgeMappedRouter = new HashMap<String,String>();//有向边和源节点、目的节点的映射(0-->(0,1))
    static final Map<Integer,ArrayList<Integer>> src2DemandRoutersPath=new HashMap<Integer, ArrayList<Integer>>();
    static final Map<Integer,Double> src2DemandRoutersPathWeight=new HashMap<Integer, Double>();
    static final Map<Integer,ArrayList<Integer>> demandRouters2dst=new HashMap<Integer, ArrayList<Integer>>();
    static final Map<Integer,Double> demandRouters2dstWeight=new HashMap<Integer, Double>();
    static final ArrayList<Integer> demandRoutersList=new ArrayList<Integer>();
    static final Map<String,ArrayList<Integer>> demand2DemandPath=new HashMap<String, ArrayList<Integer>>();
    static final Map<String,Double> demand2DemandPathWeight=new HashMap<String, Double>();

    static Stack<Integer> connectionPath=new Stack();
    static List<Stack> connectionPaths=new ArrayList<Stack>();
    static ArrayList<ArrayList<Integer>> twoNodeAllPathsList=new ArrayList<ArrayList<Integer>>();
    //static Set<Integer> demandHasNextDemandRouterSet=new HashSet<Integer>();
    static final Map<Integer,ArrayList<Integer>> demandNextDemandRoutersMap=new HashMap<Integer, ArrayList<Integer>>();
   //demand2DemandPath中ArrayList最后的一个元素用来存储权值,倒数第二个元素表示另一个必经点，最后一个元素之前的元素才是真正的路径
    public static void init(String graphContent, String condition) {
        // Map<String, String> edgeMappedRouter = new HashMap<>();//有向边和源节点、目的节点的映射(0-->(0,1))
        Map<String, String> edgeWeight = new HashMap<String,String>();//源节点、目的节点与权重的映射((0,1)--->1)
        final int TOPO_COLUMN = 4;
        String tempSrcRouterID;
        String tempDstRouterID;
        int[] weightIndex;
        StringTokenizer st1 = new StringTokenizer(graphContent, "\n");
        String str = null;
        while (st1.hasMoreTokens()) {
            String[] tempSrcAndDstID = new String[2];
            str = st1.nextToken();
            weightIndex = getPsLinePos(str, TOPO_COLUMN, ',');
            tempSrcAndDstID[0] = str.substring(weightIndex[1], weightIndex[2] - 1);
            tempSrcAndDstID[1] = str.substring(weightIndex[2], weightIndex[3] - 1);
            //tempSrcAndDstID=str.substring(weightIndex[1], weightIndex[3] - 1);
            // sbSrcDstEdgeWeight.append(tempSrcAndDstID).append("|").append(str.substring(weightIndex[3]));
            //singleEdegeWeight.add(sbSrcDstEdgeWeight.toString());
            //sbSrcDstEdgeWeight.delete(0,sbSrcDstEdgeWeight.length());
            /****************两点之间可能有多条边，将来需要修改这里********************************************/
            edgeMappedRouter.put(str.substring(weightIndex[1], weightIndex[3] - 1), str.substring(weightIndex[0], weightIndex[1] - 1));
            /****************两点之间可能有多条边，将来需要修改这里********************************************/
            //添加路由节点到set中
            tempSrcRouterID = str.substring(weightIndex[1], weightIndex[2] - 1);
            tempDstRouterID = str.substring(weightIndex[2], weightIndex[3] - 1);
            routerID.add(Integer.parseInt(tempSrcRouterID));
            routerID.add(Integer.parseInt(tempDstRouterID));
            edgeWeight.put(str.substring(weightIndex[1], weightIndex[3] - 1), str.substring(weightIndex[3]));
            //tempweight=Byte.valueOf(str.substring(weightIndex[3]));
        }
        distance = new double[routerID.size()][routerID.size()];
        Integer[] integerSortedArrayRouterID = new Integer[routerID.size()];
        sortedArrayRouterID = new int[routerID.size()];
        routerID.toArray(integerSortedArrayRouterID);
        Arrays.sort(integerSortedArrayRouterID);
        for (int i = 0; i < routerID.size(); i++) {
            sortedArrayRouterID[i] = integerSortedArrayRouterID[i];
        }
        StringBuilder srcAndDstString = new StringBuilder();
        ArrayList<Integer> temp = new ArrayList<Integer>();
        for (int i = 0; i < routerID.size(); i++) {
            for (int j = 0; j < routerID.size(); j++) {
                srcAndDstString.append(sortedArrayRouterID[i]).append(",").append(sortedArrayRouterID[j]);
                str = srcAndDstString.toString();
                if (edgeWeight.containsKey(str)) {
                    distance[i][j] = Double.parseDouble(edgeWeight.get(str));
                    temp.add(sortedArrayRouterID[j]);
                    //connectedRouterTable[i][j] = distance[i][j];
                } else {
                    distance[i][j] = Double.POSITIVE_INFINITY;
                    //connectedRouterTable[i][j] = Double.POSITIVE_INFINITY;
                }
                srcAndDstString.delete(0, srcAndDstString.length());
            }
            //neighbourTable.put(sortedArrayRouterID[i], (ArrayList) temp.clone());
            neighbourTable.put(sortedArrayRouterID[i], new ArrayList<Integer>(temp));
            //neighbourTable.put(sortedArrayRouterID[i], (ArrayList<Integer>)temp.clone());
            temp.clear();
        }
        //以上就完成了对图数据的解析，结果存放在 edgeMappedRouter、routerID、 edgeWeight。接下来解析需要计算的路径信息文件
        StringTokenizer stk2 = new StringTokenizer(condition, ",");
        //demandRouter=new int[stk2.countTokens()];
        ArrayList<String> demandRouterArrayList = new ArrayList<String>();
        demandRouterArrayList.add(stk2.nextToken());
        demandRouterArrayList.add(stk2.nextToken());
        StringTokenizer stk3 = new StringTokenizer(stk2.nextToken(), "|");
        while (stk3.hasMoreTokens()) {
            demandRouterArrayList.add(stk3.nextToken());
        }
        str = demandRouterArrayList.get(demandRouterArrayList.size() - 1).substring(0, demandRouterArrayList.get(demandRouterArrayList.size() - 1).length() - 1);
        demandRouterArrayList.set(demandRouterArrayList.size() - 1, str);
        demandRouterArray = new Integer[demandRouterArrayList.size()];
        for (int i = 0; i < demandRouterArrayList.size(); i++) {
            demandRouterArray[i] = (Integer.parseInt(demandRouterArrayList.get(i)));
            if (i>1)
            {
            demandRoutersList.add(Integer.parseInt(demandRouterArrayList.get(i)));
            }
        }
       // System.out.println();
        //以上完成了需要计算的路径信息文件的解析。结果存放在demandRouter中，前两个值是源节点、目的节点，剩下的就是需要经过的节点
    }

    public static int[] getPsLinePos(String line, int COLUMNS, char ch) {
        // 以下是为了得到每一列的pos;不在循环里面判空,节省调用
        int[] lp = new int[COLUMNS];
        lp[0] = 0; // 第一个起点是开始
        int index = 0;
        char lastChar;
        char curChar;

        int length = line.length();
        for (int j = 1; j < length; j++) {
            lastChar = line.charAt(j - 1);
            curChar = line.charAt(j);

            if (index + 1 >= COLUMNS) {
                break;
            }

            if (lastChar == ch && curChar != ch) {
                // 如果是从空格突变为非空格,那么就是起始点
                lp[++index] = j;
            }
        }
        return lp;
    }

    /*************
     * 其中srcIndex是sortedArrayRouterID中的索引，而actualDstIndex是路径中的实际索引(文件中的值)
     ********************/

    public static double dijstra2(int start, int end, int n,int[] previous) {
        double[] dist = new double[n];
        boolean[] flag = new boolean[n];
        for (int i = 0; i < n; i++) {
            dist[i] = distance[start][i];
            flag[i] = false;
            if (dist[i] < Double.POSITIVE_INFINITY) {
                previous[i] = start;
            } else {
                previous[i] = -1;
            }
        }
        dist[start] = 0;
        flag[start] = true;
        int temp = start;
        for (int i = 1; i < n && temp != end; i++) {
            double mindist = Double.POSITIVE_INFINITY;
            for (int j = 0; j < n; j++) {
                if (flag[j] == false && dist[j] < mindist) {
                    mindist = dist[j];
                    temp = j;
                }
            }
            flag[temp] = true;
            for (int j = 0; j < n; j++) {
                if (flag[j] == false && dist[temp] + distance[temp][j] < dist[j]) {
                    dist[j] = dist[temp] + distance[temp][j];
                    previous[j] = temp;
                }
            }
        }
        //shortestpathArray=previous;
        return dist[end];
    }
    public static int[] getroute(int start,int end,int[] pre){
        int[] route=new int[pre.length];
        int counter=0;
        int last=pre[end];
        for(;last!=start;last=pre[last]){
            route[counter++]=last;
        }
        int[] result=new int[counter];
        for(int i=counter-1,j=0;i>=0;i--,j++) result[j]=route[i];
        return result;
    }



    public static String searchRoute(String graphContent, String condition) throws CloneNotSupportedException {
        init(graphContent, condition);
        int srcIndex, dstIndex;
        srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[0]);
        dstIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[1]);
         ////////////////////////////////////////////////////////////
        int[] prePathArray=new int[routerID.size()];
        int[] resultPathArray;
        int middleIndex=-1;
        int vertexAmounts=routerID.size();
        ArrayList<Integer> tempList=new ArrayList<Integer>();
        double tempWeight=-1;
        for (int i=2;i<demandRouterArray.length;++i){
            middleIndex=Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[i]);
            tempWeight=dijstra2(srcIndex,middleIndex,vertexAmounts,prePathArray);
            src2DemandRoutersPathWeight.put(demandRouterArray[i],tempWeight);
            resultPathArray=getroute(srcIndex,middleIndex,prePathArray);
            //tempList.add(new Integer(demandRouterArray[0]));
            tempList.add(demandRouterArray[0]);
            for (int val:resultPathArray){
                tempList.add(val);
            }
            //tempList.add(new Integer(demandRouterArray[i]));
            tempList.add(demandRouterArray[i]);
            src2DemandRoutersPath.put(demandRouterArray[i],new ArrayList<Integer>(tempList));
            tempList.clear();
        }
        for (int i=2;i<demandRouterArray.length;++i){
            middleIndex=Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[i]);
            tempWeight=dijstra2(middleIndex,dstIndex,vertexAmounts,prePathArray);
            demandRouters2dstWeight.put(demandRouterArray[i],tempWeight);
            resultPathArray=getroute(middleIndex,dstIndex,prePathArray);
            //tempList.add(new Integer(demandRouterArray[i]));
           // tempList.add(demandRouterArray[i]);
            for (int val:resultPathArray){
                tempList.add(val);
            }
            //tempList.add(new Integer(demandRouterArray[1]));
            tempList.add(demandRouterArray[1]);
            demandRouters2dst.put(demandRouterArray[i],new ArrayList<Integer>(tempList));
            tempList.clear();
        }

         //demand2DemandPath中ArrayList最后的一个元素用来存储权值,倒数第二个元素表示另一个必经点，最后一个元素之前的元素才是真正的路径
        int demandIndex1,demandIndex2;
        Map<Integer,ArrayList<Integer>> tempMap =new HashMap<Integer, ArrayList<Integer>>();
        ArrayList<Integer> tempNextDemandRoutersList=new ArrayList<Integer>();
        StringBuilder sb=new StringBuilder();
        for (int i=2;i<demandRouterArray.length;++i){
            for (int j=2;j<demandRouterArray.length;++j){
                if (i!=j){
                    demandIndex1=Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[i]);
                    demandIndex2=Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[j]);

                    tempWeight=dijstra2(demandIndex1,demandIndex2,vertexAmounts,prePathArray);
                    if (tempWeight==Double.POSITIVE_INFINITY){
                        continue;
                    }
                    else {
                        tempNextDemandRoutersList.add(demandRouterArray[j]);
                        resultPathArray=getroute(demandIndex1,demandIndex2,prePathArray);
                        //tempList.add(demandRouterArray[i]);
                        for (int val:resultPathArray){
                            tempList.add(val);
                        }
                        tempList.add(demandRouterArray[j]);

                        //tempList.add(new Integer((int) tempWeight));
                        sb.append(demandRouterArray[i]).append(",").append(demandRouterArray[j]);
                        demand2DemandPath.put(sb.toString(), new ArrayList<Integer>(tempList));
                        demand2DemandPathWeight.put(sb.toString(),tempWeight);
                        tempList.clear();
                        sb.delete(0,sb.length());

                    }

                }
            }

            if (tempNextDemandRoutersList.size()>0){
                demandNextDemandRoutersMap.put(demandRouterArray[i],new ArrayList<Integer>(tempNextDemandRoutersList));
                 tempNextDemandRoutersList.clear();
            }

        }
        //demand2DemandPath
        /******************起始点到终点的最短路径********************************/
        //double src2dstWeight = connectedRouterTable[srcIndex][dstIndex];
        /******************起始点到终点的最短路径********************************/
        /**************************使用迪杰斯特拉求解最短路径***************************
        //src2dstWeight=Dijkstra(srcIndex,demandRouterArray[1]);
        int[] shortestPathArray=new int[routerID.size()];
        double src2dstWeight = dijstra2(srcIndex, dstIndex, routerID.size(),shortestPathArray);
        shortestPathArray=getroute(srcIndex,dstIndex,shortestPathArray);
        ArrayList<Integer> shortPathList=new ArrayList();
        //shortPathList.add(new Integer(demandRouterArray[0]));
        shortPathList.add(new Integer(demandRouterArray[0]));
        for (int i=0;i<shortestPathArray.length;++i){
             shortPathList.add(new Integer(sortedArrayRouterID[shortestPathArray[i]]));
        }
        shortPathList.add(new Integer(demandRouterArray[1]));
        //对src2dstWeight也要归一化
        //src2dstWeight=src2dstWeight/12000;

        **************************使用迪杰斯特拉求解最短路径***************************/
        /**************************测试***************************/
        //ArrayList<Integer> already=new ArrayList<Integer>();
        //ArrayList<ArrayList<Integer>> minPath=new ArrayList<ArrayList<Integer>>();
        //already.add(119);
        //statdNode和endNode直接相连的话，这个函数就不可以正常工作了
       // int aa=getReachableNodeCollection(9, 8, already, minPath);
        /**************************测试***************************/



        /*************************暴力搜索***************************/

       // ArrayList<Integer> tt=new ArrayList<Integer>();

        if(demandRouterArray.length<8){   //case1 2 3
            ArrayList<Integer> path=baoliSearch();
            StringBuilder edgeResult = new StringBuilder();
            StringBuilder s2 = new StringBuilder();
            //s2.append(demandRouterArray[0]).append(",").append(finalBestChromosome.pathUsedRouterId.get(0));
            //edgeResult.append(edgeMappedRouter.get(s2.toString())).append("|");


            for (int i = 0; i < path.size() - 1; i++) {
                s2.delete(0, s2.length());
                s2.append(path.get(i)).append(",").append(path.get(i + 1));
                edgeResult.append(edgeMappedRouter.get(s2.toString())).append("|");
            }
            edgeResult.deleteCharAt(edgeResult.length() - 1);
            //System.out.println("success");
            return edgeResult.toString();


        }
        /*************************暴力搜索***************************/
        else if (demandRouterArray.length>=8&&demandRouterArray.length<=11){ //case 4 5

            findAllPaths(demandRouterArray[0], demandRouterArray[1], neighbourTable);

            /*********暴力计算经过指定点的最小权重**************/
        Stack<Integer> currentBestPath=new Stack<Integer>();
        Stack<Integer> tempStackPath;


        double minWeight=1e8;
        double baoliTempWeight;
        int totalPathAmounts=connectionPaths.size();
        int commonNodeAmounts=0;
        for (int i = 0; i <totalPathAmounts ; i++) {
            Stack<Integer> tempStackPath2=(Stack<Integer>)(connectionPaths.get(i).clone());
            tempStackPath2.retainAll(demandRoutersList);
            commonNodeAmounts=tempStackPath2.size();
             //connectionPaths.get(i).retainAll(tempStackPath2);
            //int siz=connectionPaths.get(i).size();
                if (commonNodeAmounts==demandRouterArray.length-2){
                    baoliTempWeight=0;
                    tempStackPath=connectionPaths.get(i);
                    for (int j=0; j < tempStackPath.size()-1;) {
                        srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, tempStackPath.get(j));
                        dstIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, tempStackPath.get(++j));
                        baoliTempWeight+= distance[srcIndex][dstIndex];
                    }
                    if (baoliTempWeight<minWeight){
                        minWeight=baoliTempWeight;
                        currentBestPath=tempStackPath;
                    }
                }
            }
            //System.out.println("min weight:"+minWeight);
            StringBuilder resultBuilder=new StringBuilder();
            StringBuilder ss=new StringBuilder();
            for (int i = 0; i <currentBestPath.size()-1; i++) {
               ss.append(currentBestPath.get(i)).append(",").append(currentBestPath.get(i+1));
               resultBuilder.append(edgeMappedRouter.get(ss.toString())).append("|");
               ss.delete(0,ss.length());

            }

            resultBuilder.deleteCharAt(resultBuilder.length()-1);

            String resultString=resultBuilder.toString();
            return resultString;

            }
        else{
            String res=startGA();
            return res;

        }

    }
    public static String startGA() throws CloneNotSupportedException {
        int srcIndex, dstIndex;
        srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[0]);
        dstIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[1]);
        ////////////////////////////////////////////////////////////
        int[] prePathArray=new int[routerID.size()];
        int[] resultPathArray;
        int middleIndex=-1;
        int vertexAmounts=routerID.size();
        ArrayList<Integer> tempList=new ArrayList<Integer>();
        double tempWeight=-1;
        for (int i=2;i<demandRouterArray.length;++i){
            middleIndex=Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[i]);
            tempWeight=dijstra2(srcIndex,middleIndex,vertexAmounts,prePathArray);
            src2DemandRoutersPathWeight.put(demandRouterArray[i],tempWeight);
            resultPathArray=getroute(srcIndex,middleIndex,prePathArray);
            //tempList.add(new Integer(demandRouterArray[0]));
            tempList.add(demandRouterArray[0]);
            for (int val:resultPathArray){
                tempList.add(val);
            }
            //tempList.add(new Integer(demandRouterArray[i]));
            tempList.add(demandRouterArray[i]);
            src2DemandRoutersPath.put(demandRouterArray[i],new ArrayList<Integer>(tempList));
            tempList.clear();
        }
        for (int i=2;i<demandRouterArray.length;++i){
            middleIndex=Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[i]);
            tempWeight=dijstra2(middleIndex,dstIndex,vertexAmounts,prePathArray);
            demandRouters2dstWeight.put(demandRouterArray[i],tempWeight);
            resultPathArray=getroute(middleIndex,dstIndex,prePathArray);
            //tempList.add(new Integer(demandRouterArray[i]));
            // tempList.add(demandRouterArray[i]);
            for (int val:resultPathArray){
                tempList.add(val);
            }
            //tempList.add(new Integer(demandRouterArray[1]));
            tempList.add(demandRouterArray[1]);
            demandRouters2dst.put(demandRouterArray[i],new ArrayList<Integer>(tempList));
            tempList.clear();
        }

        //demand2DemandPath中ArrayList最后的一个元素用来存储权值,倒数第二个元素表示另一个必经点，最后一个元素之前的元素才是真正的路径
        int demandIndex1,demandIndex2;
        Map<Integer,ArrayList<Integer>> tempMap =new HashMap<Integer, ArrayList<Integer>>();
        ArrayList<Integer> tempNextDemandRoutersList=new ArrayList<Integer>();
        StringBuilder sb=new StringBuilder();
        for (int i=2;i<demandRouterArray.length;++i){
            for (int j=2;j<demandRouterArray.length;++j){
                if (i!=j){
                    demandIndex1=Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[i]);
                    demandIndex2=Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[j]);

                    tempWeight=dijstra2(demandIndex1,demandIndex2,vertexAmounts,prePathArray);
                    if (tempWeight==Double.POSITIVE_INFINITY){
                        continue;
                    }
                    else {
                        tempNextDemandRoutersList.add(demandRouterArray[j]);
                        resultPathArray=getroute(demandIndex1,demandIndex2,prePathArray);
                        //tempList.add(demandRouterArray[i]);
                        for (int val:resultPathArray){
                            tempList.add(val);
                        }
                        tempList.add(demandRouterArray[j]);

                        //tempList.add(new Integer((int) tempWeight));
                        sb.append(demandRouterArray[i]).append(",").append(demandRouterArray[j]);
                        demand2DemandPath.put(sb.toString(), new ArrayList<Integer>(tempList));
                        demand2DemandPathWeight.put(sb.toString(),tempWeight);
                        tempList.clear();
                        sb.delete(0,sb.length());

                    }

                }
            }

            if (tempNextDemandRoutersList.size()>0){
                demandNextDemandRoutersMap.put(demandRouterArray[i],new ArrayList<Integer>(tempNextDemandRoutersList));
                tempNextDemandRoutersList.clear();
            }

        }
        //int srcIndex,dstIndex;
        ArrayList<Double> populationsMaxFitnessList = new ArrayList<Double>();//存储近三十代的最大值
        int compareCount = 0;
        //int srcIndex, dstIndex;
        srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[0]);
        dstIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[1]);
        ////////////////////////////////////////////////////////////
        //int[] prePathArray=new int[routerID.size()];

        boolean isThroughed = false;//判断是否有路径可以从起点到终点,默认是bu通的
        int[] shortestPathArray=new int[routerID.size()];
        double src2dstWeight = dijstra2(srcIndex, dstIndex, routerID.size(),shortestPathArray);
        shortestPathArray=getroute(srcIndex,dstIndex,shortestPathArray);
        ArrayList<Integer> shortPathList=new ArrayList<Integer>();
        //shortPathList.add(new Integer(demandRouterArray[0]));
        shortPathList.add(demandRouterArray[0]);
        for (int i=0;i<shortestPathArray.length;++i){
            shortPathList.add(sortedArrayRouterID[shortestPathArray[i]]);
        }
        shortPathList.add(demandRouterArray[1]);

        double mutationProbability = 0, crossoverProbability = 0, smallerMutationProability = 0;
        /*
        if (routerID.size() > 100) {
            //mutationProbability=0.14;
            mutationProbability = 0.1;
            crossoverProbability = 0.99;
            //smallerMutationProability=0;

        } else {
            mutationProbability = 0.1;
            crossoverProbability = 0.99;
        }
        */
        final int populationArrayNumbers;
        final int populationIndividuals;
        final int counter;
        final int maxGenarations;
        int randomGenerateIndividuals=2;
        /*
        if (demandRouterArray.length < 5) {//针对第一测测试用例（其他测试用例应该不会有只经过两个顶点的？）
            populationArrayNumbers = 2;
            populationIndividuals = 12;
            counter = 10;
            maxGenarations = 200;
        } else if (demandRouterArray.length < 10) {//针对4测试用例
            populationArrayNumbers = 20;//10
            populationIndividuals = 200;//100
            counter = 20;
            maxGenarations = 2000;
            mutationProbability = 0.1;
            crossoverProbability = 0.99;
            randomGenerateIndividuals=2;
        } else if (demandRouterArray.length>=10&&demandRouterArray.length <=11) { //针对5测试用例
            populationArrayNumbers = 20; //3
            populationIndividuals = 200;//600
            counter = 20;
            maxGenarations = 2000;
            mutationProbability = 0.1;
            crossoverProbability = 0.99;
            randomGenerateIndividuals=2;
        } else  */
        if (demandRouterArray.length>=14&&demandRouterArray.length<=16){   //针对6、7、8测试用例
            populationArrayNumbers = 3;  //4\2000
            populationIndividuals = 1400;
            counter =10 ;
            maxGenarations = 2000;
            mutationProbability = 0.1;
            crossoverProbability = 0.99;
            randomGenerateIndividuals=20;
        }
        else if(demandRouterArray.length>16&&demandRouterArray.length<=22){  //针对9、10、15测试用例
            populationArrayNumbers = 3;  //4\2000
            populationIndividuals = 1400;
            counter = 10;
            maxGenarations = 2000;
            mutationProbability = 0.1;
            crossoverProbability = 0.99;
            randomGenerateIndividuals=20;
        }
        else if(demandRouterArray.length>22&&demandRouterArray.length<=32){   //case12、13、14
            populationArrayNumbers = 3;  //4\2000
            populationIndividuals = 1600;
            counter = 10;
            maxGenarations = 2000;
            mutationProbability = 0.1;
            crossoverProbability = 0.99;
            randomGenerateIndividuals=10;
        }
        else{         //case12、13、14
            populationArrayNumbers = 3;  //4\2000
            populationIndividuals = 2000;
            counter = 10;
            maxGenarations = 2000;
            mutationProbability = 0.1;
            crossoverProbability = 0.99;
            randomGenerateIndividuals=10;
        }



        Population[] populationArray = new Population[populationArrayNumbers];
        //int randomIndex = -1;
        //Random rand22 = new Random();
        for (int i = 0; i < populationArray.length; i++) {
            /*交叉概率取0.6至0.95之间的值；变异概率取0.001至0.01之间的值。*/
            //populationArray[i] = new Population(80, 500, 0.85, 0.6, 0.01, 0.001);
            populationArray[i] = new Population(populationIndividuals, maxGenarations, crossoverProbability, 0.15, mutationProbability, 0.0001, shortPathList);
            populationArray[i].populationInit(isThroughed, randomGenerateIndividuals); //500,2000

        }
        double bestFitnessAmongPopulations = 0;
        int bestFitnessPopulatinIndex = 0;
        int currentBestChromosomeIncludeDemandRouters = 0;
        Chromosome finalBestChromosome;
        int populationSize = populationArray[0].popSize;
        //boolean startMove=false;
        Random rand = new Random();
        // Chromosome tempChromosome;
        //int bestIndex;
        //double bestFitValue;
        //finalBestChromosome=populationArray[bestFitnessPopulatinIndex].parentChromosome[ populationSize];
        double updateMutation = mutationProbability;
        ArrayList<Integer> populationList = new ArrayList<Integer>();
        int worstIndex, primaryIndex, sourcePopulationIndex, destinationPopulationIndex, index2;
        int populationNumbers = populationArray.length;
        for (int i = 0; i < populationArray[0].MAX_GEN; i++) {
            /***********************************种群进化***************************************/
            for (int k = 0; k < populationArray.length; k++) {
                populationArray[k].evolutionMethod2(updateMutation);//开始进化
                populationList.add(k);
            }
            /***********************************种群进化***************************************/
            bestFitnessAmongPopulations = populationArray[0].parentChromosome[populationSize].chromosomeFitness;
            bestFitnessPopulatinIndex = 0;
            for (int j = 1; j < populationArray.length; j++) {
                if (bestFitnessAmongPopulations < populationArray[j].parentChromosome[populationSize].chromosomeFitness) {
                    bestFitnessAmongPopulations = populationArray[j].parentChromosome[populationSize].chromosomeFitness;
                    bestFitnessPopulatinIndex = j;
                }
            }
            finalBestChromosome = populationArray[bestFitnessPopulatinIndex].parentChromosome[populationSize];
            currentBestChromosomeIncludeDemandRouters = finalBestChromosome.includedRoutersInPath;
            /*************************************如果种群30代没有明显变化，停止迭代*********************************/
            //pathSet=new HashSet<>(finalBestChromosome.includedRoutersInPath);
            if (currentBestChromosomeIncludeDemandRouters == demandRouterArray.length - 2 && compareCount == counter &&
                    Collections.max(populationsMaxFitnessList) - Collections.min(populationsMaxFitnessList) < 5) {
                //System.out.println("break");
                break;
            } else if (compareCount == counter) {
                if (routerID.size() >70 && Collections.max(populationsMaxFitnessList) - Collections.min(populationsMaxFitnessList) < 1) {
                    updateMutation = updateMutation * 1.05 < 0.6 ? updateMutation * 1.05 : updateMutation;
                }
                //counter=0;
                populationsMaxFitnessList.clear();
                compareCount = 0;

            }
            /*
            if (populationsMaxFitnessList.size()>15&& Collections.max(populationsMaxFitnessList)-Collections.min(populationsMaxFitnessList)<1){
                updateMutation=updateMutation*1.05<0.6?updateMutation*1.05:updateMutation;
            }
            */
            /*************************************如果种群30代没有明显变化，停止迭代*********************************/
            //种群最优个体迁移
            index2 = rand.nextInt(populationList.size());
            sourcePopulationIndex = populationList.get(index2);
            primaryIndex = sourcePopulationIndex;
            populationList.remove(index2);
            index2 = rand.nextInt(populationList.size());
            destinationPopulationIndex = populationList.get(index2);
            populationList.remove(index2);
            worstIndex = populationArray[destinationPopulationIndex].worstChromosomeIndex;
            populationArray[destinationPopulationIndex].parentChromosome[worstIndex] = (Chromosome)
                    populationArray[sourcePopulationIndex].parentChromosome[populationSize].clone();
            while (populationList.size() > 0) {
                //index2=rand.nextInt(populationList.size());
                sourcePopulationIndex = destinationPopulationIndex;
                //primaryIndex=sourcePopulationIndex;
                //populationList.remove(index2);
                index2 = rand.nextInt(populationList.size());
                destinationPopulationIndex = populationList.get(index2);
                populationList.remove(index2);
                worstIndex = populationArray[destinationPopulationIndex].worstChromosomeIndex;
                populationArray[destinationPopulationIndex].parentChromosome[worstIndex] = (Chromosome)
                        populationArray[sourcePopulationIndex].parentChromosome[populationSize].clone();

            }
            sourcePopulationIndex = destinationPopulationIndex;
            destinationPopulationIndex = primaryIndex;
            worstIndex = populationArray[destinationPopulationIndex].worstChromosomeIndex;
            populationArray[destinationPopulationIndex].parentChromosome[worstIndex] = (Chromosome)
                    populationArray[sourcePopulationIndex].parentChromosome[populationSize].clone();
            //startMove=false;

            //种群最优个体迁移
            //populationArray[0].repairFunctionForCrossover(finalBestChromosome);

            populationsMaxFitnessList.add(bestFitnessAmongPopulations);
            compareCount++;
            //System.out.println(i+":generation fitness:" + populationArray[bestFitnessPopulatinIndex].parentChromosome[ populationSize].chromosomeFitness);

        }
        /*******************迭代结束，输出计算结果******************************************************************************/
        finalBestChromosome = populationArray[bestFitnessPopulatinIndex].parentChromosome[populationSize];
        StringBuilder s2 = new StringBuilder();
        //ArrayList<Integer> resultEdge=new ArrayList<>();
        // String xx;

        //System.out.println("maltiple populations generation fitness:" + populationArray[bestFitnessPopulatinIndex].parentChromosome[ populationSize].pathTotalWeight);
        Set<Integer> test = new HashSet<Integer>(finalBestChromosome.pathUsedRouterId);
        if (finalBestChromosome.includedRoutersInPath == demandRouterArray.length - 2 && test.size() ==
                finalBestChromosome.pathUsedRouterId.size()) {//如果实际路径包含全部指定点
            StringBuilder edgeResult = new StringBuilder();
            if (!finalBestChromosome.pathUsedRouterId.get(0).equals(demandRouterArray[0])) {
                s2.append(demandRouterArray[0]).append(",").append(finalBestChromosome.pathUsedRouterId.get(0));
                edgeResult.append(edgeMappedRouter.get(s2.toString())).append("|");
            }

            for (int i = 0; i < finalBestChromosome.pathUsedRouterId.size() - 1; i++) {
                s2.delete(0, s2.length());
                s2.append(finalBestChromosome.pathUsedRouterId.get(i)).append(",").append(finalBestChromosome.pathUsedRouterId.get(i + 1));
                edgeResult.append(edgeMappedRouter.get(s2.toString())).append("|");
            }
            edgeResult.deleteCharAt(edgeResult.length() - 1);
            //System.out.println("success");
            return edgeResult.toString();
        } else {
            //System.out.println("fail");
            //System.out.println(finalBestChromosome.includedRoutersInPath );
            return "NA";
        }
    }












    //statdNode和endNode直接相连的话，这个函数就不可以正常工作了
    public static  int getReachableNodeCollection(Integer statdNode,Integer endNode,ArrayList<Integer> alreadyUsedKey,
                                                  ArrayList<ArrayList<Integer>> shortPath){
        ArrayList<ArrayList<Integer>> reachableNodeCollection=new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> reachableNode=new ArrayList<Integer>();
        ArrayList<Integer> tempNeighbour;
        final ArrayList<Integer> keySetList=new ArrayList<Integer>();
        for (Integer key:neighbourTable.keySet()){
            if (!key.equals(endNode))
            {
                keySetList.add(key);
            }
        }
        keySetList.removeAll(alreadyUsedKey);//删除路径中已经使用过的key
        Integer key;
        tempNeighbour=new ArrayList<Integer>();
        for (int i = 0; i <keySetList.size(); i++) {
            key=keySetList.get(i);
            tempNeighbour.addAll(neighbourTable.get(key));
            if ((tempNeighbour.contains(endNode))){
                reachableNode.add(key);
                //continue;

            }
            tempNeighbour.clear();

        }
        reachableNodeCollection.add(new ArrayList<Integer>(reachableNode));//到最后一个节点的节点集合存在第一个位置，将来获得路径是要逆序访问
        ArrayList<Integer> copyOfReachableNode;
        ArrayList<Integer> currentKeySet=new ArrayList<Integer>(keySetList);
        currentKeySet.removeAll(reachableNode);
        Integer NodeId;
        copyOfReachableNode=new ArrayList<Integer>();
        boolean findStartNode=false;
        Integer startNodeId=statdNode;
        int nextKeySetSize,currentKeySetSize;
        while(currentKeySet.size()>0&&!reachableNode.contains(startNodeId)){
            currentKeySetSize=currentKeySet.size();
            copyOfReachableNode.addAll(reachableNode);
            reachableNode.clear();
            for (int i = 0; i <currentKeySet.size() ; i++) {
                key=currentKeySet.get(i);

                tempNeighbour.addAll(neighbourTable.get(key));
                for (int j = 0; j <copyOfReachableNode.size() ; j++) {
                    NodeId=copyOfReachableNode.get(j);
                    if (tempNeighbour.contains(NodeId)){
                        reachableNode.add(key);
                        if (key.equals(startNodeId)){
                            findStartNode=true;//如果已经找到起始点，就跳出搜索
                        }
                        break;
                    }
                }
                tempNeighbour.clear();
                if (findStartNode){
                    break;
                }



            }
            reachableNodeCollection.add(new ArrayList<Integer>(reachableNode));
            currentKeySet.removeAll(reachableNode);
            nextKeySetSize=currentKeySet.size();
            if (nextKeySetSize==currentKeySetSize){//没有路径
                return -1;

            }
            copyOfReachableNode.clear();
            if (findStartNode){
                break;
            }
        }
        //以上只是找到了所有可以到达终点的点，但是从起点经过那些点可以到达终点还没得出，以下主要就是求交集

        Integer tempStartNode=statdNode;
        Map<Integer,ArrayList<Integer>> newReachableMap=new HashMap<Integer, ArrayList<Integer>>();
        ArrayList<Integer> preReachableNodeCollection;
        ArrayList<Integer> copyOfOneReachableNodeCollection=new ArrayList<Integer>();
        ArrayList<Integer> tt=new ArrayList<Integer>();
        tt.add(startNodeId);
        //ArrayList<Integer> updateReachableNodeCollection=new ArrayList<>();
        reachableNodeCollection.get(reachableNodeCollection.size()-1).retainAll(tt);//删除最后一组元素中除了起始点以外的节点
        ArrayList<Integer> updateReachableNodeCollection=new ArrayList<Integer>(reachableNodeCollection.get(reachableNodeCollection.size()-1));
        for (int i = reachableNodeCollection.size()-2; i>=0 ; i--) {
            preReachableNodeCollection=new ArrayList<Integer>(updateReachableNodeCollection);
            updateReachableNodeCollection.clear();
            //preReachableNodeCollection.addAll(reachableNodeCollection.get(i+1));
            for (int j = 0; j <preReachableNodeCollection.size(); j++) {
                copyOfOneReachableNodeCollection.addAll(reachableNodeCollection.get(i));
                copyOfOneReachableNodeCollection.retainAll(neighbourTable.get(preReachableNodeCollection.get(j)));
                newReachableMap.put(preReachableNodeCollection.get(j), new ArrayList<Integer>(copyOfOneReachableNodeCollection));
                updateReachableNodeCollection.addAll(new ArrayList<Integer>(copyOfOneReachableNodeCollection));
                copyOfOneReachableNodeCollection.clear();

            }

            //reachableNodeCollection.get(i)=new ArrayList<>(updateReachableNodeCollection);
        }


        findAllPaths(startNodeId, endNode, newReachableMap);

        for (int i = 0; i <connectionPaths.size() ; i++) {
            connectionPaths.get(i).push(endNode);
            connectionPaths.get(i).add(0, startNodeId);
            shortPath.add(new ArrayList<Integer>(connectionPaths.get(i)));
        }
        System.out.println();
        return 1;



    }

    public static void findAllPaths(Integer nodeId,Integer targetNodeId, Map<Integer,ArrayList<Integer>> reachMap) {

            for (Integer nextNode : reachMap.get(nodeId)) {
                if (nextNode.equals(targetNodeId)) {
                    Stack temp = new Stack();
                    for (Integer node1 : connectionPath) {
                        temp.add(node1);
                    }
                    temp.push(demandRouterArray[1]);
                    temp.add(0, demandRouterArray[0]);
                    connectionPaths.add(temp);
                } else if (!connectionPath.contains(nextNode)) {
                    connectionPath.push(nextNode);
                    findAllPaths(nextNode, targetNodeId,reachMap);
                    connectionPath.pop();
                }
            }
        }


    public static double twoNodeShortestPath(int startNode,int endNode, ArrayList<Integer> shortPathList){
        /**************************使用迪杰斯特拉求解最短路径***************************/
        //src2dstWeight=Dijkstra(srcIndex,demandRouterArray[1]);
        ArrayList<Integer> copyOfPath=new ArrayList<Integer>();
        int srcIndex,dstIndex;
        int[] shortestPathArray=new int[routerID.size()];
        srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, startNode);
        dstIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, endNode);
        double src2dstWeight = dijstra2(srcIndex, dstIndex, routerID.size(),shortestPathArray);
        if(src2dstWeight!=Double.POSITIVE_INFINITY) {
            shortestPathArray = getroute(srcIndex, dstIndex, shortestPathArray);
            //shortPathList.add(new Integer(demandRouterArray[0]));
            //shortPathList.add(new Integer(demandRouterArray[0]));
            for (int i = 0; i < shortestPathArray.length; ++i) {
                copyOfPath.add(new Integer(sortedArrayRouterID[shortestPathArray[i]]));
            }
            copyOfPath.add(endNode);
            ArrayList<Integer> copyCopy=new ArrayList<Integer>(copyOfPath);
            copyCopy.retainAll(shortPathList);
            if (copyCopy.size()==0){
                shortPathList.addAll(copyOfPath);
                return src2dstWeight ;
            }
            else {
                return -1;
            }


        }
        else {
            return -1;
        }
        //对src2dstWeight也要归一化
        //src2dstWeight=src2dstWeight/12000;

        /**************************使用迪杰斯特拉求解最短路径***************************/
    }
    public static ArrayList<Integer> baoliSearch(){

        //ArrayList<ArrayList<Integer>> possableGroups=new ArrayList<ArrayList<Integer>>();
        int [] a=new int[demandRouterArray.length-2];
        for (int i = 2; i <demandRouterArray.length; i++) {
            a[i-2]=demandRouterArray[i];
        }
        //int n=2;
        permutation(a,0,a.length-1);
        int amount=demandRouterArray.length;
        double minWeight=1e8;
        double returnValue;
        boolean flag=false;
        ArrayList<Integer> bestPath=new ArrayList<Integer>();
        for (int i = 0; i <permutaion.size() ; i++) {
            ArrayList<Integer> path=new ArrayList<Integer>();
            ArrayList<Integer> tempPath=new ArrayList<Integer>(permutaion.get(i));
            double weight=0;
            for (int j = 0; j <amount-1 ; j++) {
                //ArrayList<Integer> copyOfPath=new ArrayList<>(path);
               returnValue=twoNodeShortestPath(tempPath.get(j),tempPath.get(j+1),path);
                if (returnValue!=-1){
                    weight+=returnValue;
                    flag=true;
                }
                else {
                    weight=1e6;
                    flag=false;
                    break;
                }


            }
            if (flag){
                if (weight<minWeight){
                    bestPath.clear();
                    bestPath.addAll(path);
                    minWeight=weight;
                }
            }




        }
        bestPath.add(0, demandRouterArray[0]);

        //System.out.println();
        return bestPath;
    }

    public static void permutation(int[] str,int first,int end){
        //输出str[first..end]的所有排列方式
        if(first == end) {
           //输出一个排列方式
            ArrayList<Integer> onePermutations=new ArrayList<Integer>();
            onePermutations.add(demandRouterArray[0]);
            for(int j=0; j<= end ;j++) {
                onePermutations.add(str[j]);
                //System.out.print(str[j]+",");

            }
            onePermutations.add(demandRouterArray[1]);

            permutaion.add(onePermutations);

            //System.out.println();
            permutations++;
        }

        for(int i = first; i <= end ; i++) {
            swap(str, i, first);
            permutation(str, first+1, end);  //固定好当前一位，继续排列后面的
            swap(str, i, first);
        }
    }

    public static void swap(int[] str,int i,int first){
        int tmp;
        tmp = str[first];
        str[first] = str[i];
        str[i] = tmp;
    }






    static class Chromosome implements Cloneable {

        private double chromosomeFitness = 0;
        private ArrayList<Integer> pathUsedRouterId = new ArrayList<Integer>();//实现变长染色体
        private double pathTotalWeight = 0;
        private int includedRoutersInPath = 0;
        public Chromosome(boolean success) {
            int tryCounter = -1;
            int returnValue = generateRouterPath4();
            if (returnValue == -1) {//第一次尝试生成路径失败
                ++tryCounter;
                while (tryCounter < 50000) {
                    returnValue = generateRouterPath4();
                    if (returnValue == -1) {
                        tryCounter++;//生成路径失败次数
                    } else {
                        success = true;
                        break;
                    }
                }
            }
            //calPrimaryPopulationWeightAndSetWeightArguments(map);
        }
        public Chromosome(ArrayList<Integer> shortPath,double totalWeight){
            double currentWeight = 0.0;
            for (int i=0;i<shortPath.size();++i){
                    //pathUsedRouterId.add(new Integer((Integer) (shortPath.get(i))));
                    pathUsedRouterId.add(shortPath.get(i));
            }


            for (int i2 = 2; i2 < demandRouterArray.length; i2++) {
                if (this.pathUsedRouterId.indexOf(demandRouterArray[i2]) > -1) {
                    ++includedRoutersInPath;
                }
            }
           pathTotalWeight=totalWeight;
            //对染色体的总权重归一化.3.27
            //pathTotalWeight=pathTotalWeight/12000;
            //对染色体的总权重归一化
            //dstIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[1]);
            //srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, pathUsedRouterId[i]);
            //srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, pathUsedRouterId.get(i));
            //pathTotalWeight += distance[srcIndex][dstIndex];
            /******************************************************************/

           // return 1;
        }


        public int generateRouterPath4() {
            //ArrayList<Integer> databaseList = new ArrayList<>();//防止路径中包含多个相同节点
            //int srcIndex, dstIndex;//
            pathUsedRouterId.clear();
            pathTotalWeight=0;
            double currentWeight = 0.0;
            Random rand = new Random();
            int breakCounter = 0;
            //pathUsedRouterId.add(demandRouterArray[0]);
            int selectedIndex = rand.nextInt(neighbourTable.get(demandRouterArray[0]).size());
            Integer currentID = neighbourTable.get(demandRouterArray[0]).get(selectedIndex);
            //Integer preIndex=-1;
            pathUsedRouterId.add(demandRouterArray[0]);
            pathUsedRouterId.add(currentID);
            while (!pathUsedRouterId.get(pathUsedRouterId.size() - 1).equals(demandRouterArray[1])) {
                try {
                    selectedIndex = rand.nextInt(neighbourTable.get(currentID).size());//可能会有异常产生，因为元素个数可能为0.
                    // preIndex=currentID;
                    currentID = neighbourTable.get(currentID).get(selectedIndex);
                } catch (Exception ex) {
                    // System.out.println(237);
                }
                pathUsedRouterId.add(currentID);
                breakCounter++;
                if (breakCounter == 10000) {
                    // System.out.println("未找到起点到终点的路径!265行");
                    return -1;
                }
            }
            /******************************************************************/
            int srcIndex, dstIndex;//
            //double currentWeight = 0.0;
            //double weightAlphaArgument;
            // int includedRoutersInPath=-1;
            for (int i2 = 2; i2 < demandRouterArray.length; i2++) {
                if (this.pathUsedRouterId.indexOf(demandRouterArray[i2]) > -1) {
                    ++includedRoutersInPath;
                }
            }
            int i = 0;
            for (; i < pathUsedRouterId.size() - 1; ) {
                srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, pathUsedRouterId.get(i));
                dstIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, pathUsedRouterId.get(++i));
                pathTotalWeight += distance[srcIndex][dstIndex];
            }

            //对染色体的总权重归一化.3.27
            //pathTotalWeight=pathTotalWeight/12000;
            //对染色体的总权重归一化
            //dstIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[1]);
            //srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, pathUsedRouterId[i]);
            //srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, pathUsedRouterId.get(i));
            //pathTotalWeight += distance[srcIndex][dstIndex];
            //weightArgumentsMap.put((Integer)demandRouterArray.length-2,1);
            /******************************************************************/
            return 1;
        }

        public void initWeightArguments(double[] weightArguments) {
            weightArguments[demandRouterArray.length - 2] = 1.0;
            weightArguments[demandRouterArray.length - 3] = 8.0;
           for (int i=weightArguments.length-3;i>=0;--i){
               weightArguments[i]=1.5*weightArguments[i+1];
           }
        }



        public void calTotalWeightAndIncludeRouters() {
            includedRoutersInPath = 0;
            int srcIndex, dstIndex;//
            //double currentWeight = 0.0;
            //double weightAlphaArgument;
            // int includedRoutersInPath=-1;
            for (int i2 = 2; i2 < demandRouterArray.length; i2++) {
                if (this.pathUsedRouterId.indexOf(demandRouterArray[i2]) > -1) {
                    ++includedRoutersInPath;
                }
            }
            srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, demandRouterArray[0]);
            dstIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, pathUsedRouterId.get(0));
            if (srcIndex!=dstIndex){
                pathTotalWeight = distance[srcIndex][dstIndex];
            }
            else {
                pathTotalWeight=0;
            }

            int i = 0;
            for (; i < pathUsedRouterId.size() - 1; ) {
                srcIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, pathUsedRouterId.get(i));
                dstIndex = Arrays.binarySearch(sortedArrayRouterID, 0, sortedArrayRouterID.length, pathUsedRouterId.get(++i));
                pathTotalWeight += distance[srcIndex][dstIndex];
            }
            //对染色体的总权重归一化.3.27
            //pathTotalWeight=pathTotalWeight/12000;
            //对染色体的总权重归一化

        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            Chromosome chromosome = (Chromosome) super.clone();
            //chromosome.pathUsedRouterId = (ArrayList) this.pathUsedRouterId.clone();
            chromosome.pathUsedRouterId=new ArrayList<Integer>(this.pathUsedRouterId);
            //chromosome.currentPathRouterAmount = this.currentPathRouterAmount;
            chromosome.chromosomeFitness = this.chromosomeFitness;
            chromosome.pathTotalWeight = this.pathTotalWeight;
            //chromosome.totalWeight=this.totalWeight;
            //chromosome.weightArguments=this.weightArguments;
            chromosome.includedRoutersInPath = this.includedRoutersInPath;
            //chromosome.MAX_VALUE = this.MAX_VALUE;
            return chromosome;
        }
    }

    static class Population {
        private Chromosome[] parentChromosome;
        private Chromosome[] childChromosome;
        private double biggerCrossoverProbability;
        private double smallerCrossoverProbability;
        private double biggerMutationProbability;
        private double smallerMutationProbability;
        private double averageFitness = 0;
        private double bestChromosomeFiteness;//3.27
        private int bestChromosomeIndex;//3.27
        private int worstChromosomeIndex;//3.27
        static double[] weightArguments = new double[demandRouterArray.length - 1];
        public int MAX_GEN;
        public int popSize;
        public ArrayList<Integer> src2dstShorPath;

        public Population(int popSize, int MAX_GEN, double biggerCrossoverProbability, double smallerCrossoverProbability,
                          double bigerMutationProbability, double smallerMutationProbability,ArrayList<Integer> shortPath
        ) throws CloneNotSupportedException {
            this.popSize = popSize;
            this.MAX_GEN = MAX_GEN;
            this.biggerCrossoverProbability = biggerCrossoverProbability;
            this.smallerCrossoverProbability = smallerCrossoverProbability;
            this.biggerMutationProbability = bigerMutationProbability;
            this.smallerMutationProbability = smallerMutationProbability;
            parentChromosome = new Chromosome[popSize + 1];//存储本代最有个体
            childChromosome = new Chromosome[popSize + 1];//存储上代最优个体
            src2dstShorPath=shortPath;
            //totalFitness=0;

            /**************得到分别过x个必经节点的所有染色体的索引，updateWeightArguments中使用***************************************/
        }

        public int populationInit(boolean isThrough, int randomChromosomeAmounts) throws CloneNotSupportedException {
            Random rand=new Random();
            ArrayList<Integer> demandRouters=new ArrayList<Integer>();
            //ArrayList<ArrayList<Integer>> groups = new ArrayList();
              /*
            for (int i = 0; i < demandRouterArray.length - 1; i++) {
                groups.add(new ArrayList<Integer>());
                //groups[i]=new ArrayList<Integer>();//经过i个必经节点的染色体的在population中的索引存在group中
            }
            */
            for (int i = 0; i < randomChromosomeAmounts; i++) {
                parentChromosome[i] = new Chromosome(isThrough);
                if (!isThrough){
                    break;
                }
            }
            randomChromosomeAmounts=0;
            parentChromosome[0].initWeightArguments(weightArguments);
            int demandRouterID=-1,nextDemandRouterID=-1;
            int demandSize=demandRouterArray.length-2;
            ArrayList<Integer> wonderfulPath=new ArrayList<Integer>();
            int  randomIndex2;
            StringBuilder twoDemandRoutersStringBuilder=new StringBuilder();
            for (int i2=2;i2<demandSize+2;++i2){
                demandRouters.add(demandRouterArray[i2]);
            }

            double tempWeight=0;
            String twoDemandsString;
            ArrayList<Integer> mapIList;
            boolean flag;
            boolean primaryCanReachOtherNode;
            for (int i=randomChromosomeAmounts;i<popSize;++i){
                flag=false;
                primaryCanReachOtherNode=false;
                randomIndex2 = rand.nextInt(demandRouters.size());
                demandRouterID=demandRouters.get(randomIndex2);
               //demandRouterID=Integer.valueOf(20);
                wonderfulPath.addAll(src2DemandRoutersPath.get(demandRouterID));
                tempWeight=src2DemandRoutersPathWeight.get(demandRouterID);

                while (demandNextDemandRoutersMap.containsKey(demandRouterID)){//此节点能到达其他节点，不能到达呢？

                    primaryCanReachOtherNode=true;
                    /**************************************************/
                    //demandRouterID=Integer.valueOf(20);
                    /**************************************************
                    //wonderfulPath.addAll(src2DemandRoutersPath.get(demandRouterID));
                    tempDemandRouterList.remove((Object) demandRouterID);
                    mapIList=new ArrayList<>(demandNextDemandRoutersMap.get(demandRouterID));
                    mapIList.retainAll(tempDemandRouterList);
                    try {
                        randomIndex2 = rand.nextInt(mapIList.size());
                    }
                    catch (IllegalArgumentException ex){
                        wonderfulPath.addAll(demandRouters2dst.get(demandRouterID));
                        tempWeight+=demandRouters2dstWeight.get(demandRouterID);
                        break;
                    }
                    ************/
                    mapIList=new ArrayList<Integer>(demandNextDemandRoutersMap.get(demandRouterID));
                    randomIndex2 = rand.nextInt(mapIList.size());
                    nextDemandRouterID=mapIList.get(randomIndex2);
                    while (wonderfulPath.contains(nextDemandRouterID)){
                        mapIList.remove(randomIndex2);
                        try {
                            randomIndex2=rand.nextInt(mapIList.size());
                        }
                        catch (Exception ex){
                            //wonderfulPath.addAll(demandRouters2dst.get(demandRouterID));
                            tempWeight+=demandRouters2dstWeight.get(wonderfulPath.get(wonderfulPath.size()-1));
                            wonderfulPath.addAll(demandRouters2dst.get(wonderfulPath.get(wonderfulPath.size()-1)));
                            //tempWeight+=demandRouters2dstWeight.get(wonderfulPath.get(wonderfulPath.size()-1));
                            flag=true;
                            //tempDemandRouterList.clear();
                            break;

                        }
                        nextDemandRouterID=mapIList.get(randomIndex2);
                    }
                    if (flag){
                        break;
                    }
                    else {
                        //tempDemandRouterList.remove((Object) nextDemandRouterID);

                        //tempWeight+=demand2DemandPathWeight.get(demandRouterID);
                        twoDemandRoutersStringBuilder.append(demandRouterID).append(",").append(nextDemandRouterID);
                        twoDemandsString = twoDemandRoutersStringBuilder.toString();
                        wonderfulPath.addAll(demand2DemandPath.get(twoDemandsString));
                        tempWeight += demand2DemandPathWeight.get(twoDemandsString);
                        demandRouterID = nextDemandRouterID;
                        twoDemandRoutersStringBuilder.delete(0, twoDemandRoutersStringBuilder.length());
                    }
                }

                if (!primaryCanReachOtherNode){
                    wonderfulPath.addAll(demandRouters2dst.get(demandRouterID));
                    tempWeight=demandRouters2dstWeight.get(demandRouterID)+src2DemandRoutersPathWeight.get(demandRouterID);
                }
                else if (!flag){
                    wonderfulPath.addAll(demandRouters2dst.get(demandRouterID));
                    tempWeight+=demandRouters2dstWeight.get(demandRouterID);

                }
                /**************************************************************************************/
                /********************************去除染色体中在起始点、必经点、以及终点之间的环*****************
                ArrayList<Integer> demandRoutersIndex=new ArrayList();
                Integer temp11;
                demandRoutersIndex.add(0);
                for (int i3 = 0; i3 <wonderfulPath.size(); i3++) {
                    temp11=wonderfulPath.get(i3);
                    if (demandRoutersList.contains(temp11)){
                        demandRoutersIndex.add(i3);
                    }
                }
                demandRoutersIndex.add(wonderfulPath.size()-1);
                int startIndex,endIndex,onceDeleteRouters=0,cool=0;
                for (int i3=0;i3<demandRoutersIndex.size()-1;++i3){
                    startIndex=demandRoutersIndex.get(i3)-onceDeleteRouters;
                    endIndex=demandRoutersIndex.get(i3+1)-onceDeleteRouters;
                    //onceDeleteRouters=0;
                    while (startIndex<endIndex) {
                        cool=0;
                        for (int i2 = endIndex; i2 > startIndex; i2--) {

                            if (wonderfulPath.get(startIndex).equals(wonderfulPath.get(i2))) {
                                onceDeleteRouters+=i2-startIndex;
                                cool=i2-startIndex;
                                for (int j = 0; j < i2 - startIndex; j++) {
                                    wonderfulPath.remove(startIndex + 1);

                                }
                                break;
                            }
                        }
                        startIndex++;
                        endIndex-=cool;
                    }
                }


                ********************************去除染色体中在起始点、必经点、以及终点之间的环*****************/
                //repairFunction1(wonderfulPath);
                /**********************************************************************************/
                parentChromosome[i] = new Chromosome(wonderfulPath,tempWeight);
                wonderfulPath.clear();
              }
            /**************得到分别过x个必经节点的所有染色体的索引***************************************/
            //parentChromosome[0].initWeightArguments(weightArguments);

            /********************************更新种群的权值参数************************************************************/
            //updateWeightArguments(groups, weightArguments, parentChromosome);
            //primaryUpdateWeightArguments(groups, weightArguments, parentChromosome);
            for (int i = 0; i < popSize; i++) {
                /****************计算适应度*****************/
                parentChromosome[i].chromosomeFitness = (1 / (weightArguments[parentChromosome[i].includedRoutersInPath] *
                        parentChromosome[i].pathTotalWeight))*(Math.pow(2,parentChromosome[i].includedRoutersInPath+10));
                /****************计算适应度*****************/
            }
            primaryPopulationEvolution();
            return 1;

        }

        public void repairFunction1(ArrayList<Integer> c1) {
            int currentSize = -1;
            for (int i = 0; i < c1.size(); i++) {
                currentSize = c1.size();
                for (int j = currentSize - 1; j > i; j--) {
                    if (c1.get(j).equals(c1.get(i))) {
                        {//不能使用==来比较，因为c1中存储的是Integer，是引用，使用==实际上比较的是
                            for (int k = 0; k < j - i; k++) {                                //两个引用的地址值是否相等，并不是对象中的int型值是否相等
                                c1.remove(i + 1);
                            }
                            break;
                        }
                    }
                }
            }


        }


        public int crossoverMethod(Chromosome inputC1, Chromosome inputC2, Random rand) throws CloneNotSupportedException {//final定义内联函数
            ArrayList<String> potentialCrossOverPoints = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();
            int randomCrossOverPoint;
            Integer tempSwapValue;
            int c1RemainLength, c2RemainLegth , shorterlength, c1CrossoverPointsIndex, c2CrossoverPointsIndex;
            for (int i = 0; i < inputC1.pathUsedRouterId.size(); i++) {
                for (int j = 0; j < inputC2.pathUsedRouterId.size(); j++) {
                    if (inputC1.pathUsedRouterId.get(i).equals(inputC2.pathUsedRouterId.get(j))) {
                        sb.append(i).append(",").append(j);
                        potentialCrossOverPoints.add(sb.toString());
                        sb.delete(0, sb.length());
                    }
                }
            }
            if (potentialCrossOverPoints.size() == 0) {
                // System.out.println("交叉失败，两条染色体没有共同基因点。936");
                return -1;
            }
            randomCrossOverPoint = rand.nextInt(potentialCrossOverPoints.size());
            StringTokenizer st = new StringTokenizer(potentialCrossOverPoints.get(randomCrossOverPoint), ",");
            c1CrossoverPointsIndex = Integer.parseInt(st.nextToken());
            c2CrossoverPointsIndex = Integer.parseInt(st.nextToken());
            c1RemainLength = inputC1.pathUsedRouterId.size() - c1CrossoverPointsIndex - 1;
            c2RemainLegth = inputC2.pathUsedRouterId.size() - c2CrossoverPointsIndex - 1;
            shorterlength = c1RemainLength < c2RemainLegth ? c1RemainLength : c2RemainLegth;
            for (int i = 0; i < shorterlength; i++) {
                tempSwapValue = inputC1.pathUsedRouterId.get(++c1CrossoverPointsIndex);
                inputC1.pathUsedRouterId.set(c1CrossoverPointsIndex, inputC2.pathUsedRouterId.get(++c2CrossoverPointsIndex));
                inputC2.pathUsedRouterId.set(c2CrossoverPointsIndex, tempSwapValue);
            }
            int temp;
            if (shorterlength == c1RemainLength) {
                temp = inputC2.pathUsedRouterId.size();
                for (int i = c2CrossoverPointsIndex + 1; i < temp; i++) {
                    //inputC1.pathUsedRouterId.add(inputC2.pathUsedRouterId.get(i));
                    inputC1.pathUsedRouterId.add(inputC2.pathUsedRouterId.get(c2CrossoverPointsIndex + 1));
                    inputC2.pathUsedRouterId.remove(c2CrossoverPointsIndex + 1);
                }


            } else {
                temp = inputC1.pathUsedRouterId.size();
                for (int i = c1CrossoverPointsIndex + 1; i < temp; i++) {
                    //inputC1.pathUsedRouterId.add(inputC2.pathUsedRouterId.get(i));
                    inputC2.pathUsedRouterId.add(inputC1.pathUsedRouterId.get(c1CrossoverPointsIndex + 1));
                    inputC1.pathUsedRouterId.remove(c1CrossoverPointsIndex + 1);
                }
            }

            //outputC1 = (Chromosome) inputC1.clone();
            //outputC2 = (Chromosome) inputC2.clone();
            /*******************************************************************************************/
            //适应度函数还未计算,这里只是更新了染色体的路径
            /*******************************************************************************************/
            return 1;
        }

        public int mutationMethod(Chromosome c1, Random rand) {
            rand = new Random();
            ArrayList<Integer> backupPath = new ArrayList<Integer>(c1.pathUsedRouterId);
            int beforeMutationIncludeRouters=c1.includedRoutersInPath;
            double beforeMutationTotalWeight=c1.pathTotalWeight;
            double beforeMutationChrosomeFiteness=c1.chromosomeFitness;
            ArrayList<Integer> demandRouters = new ArrayList<Integer>();
            ArrayList<Integer> intersectionList = new ArrayList<Integer>();
            Map<Integer,ArrayList<Integer>> remainNeighborTable=neighbourTable;
            for (int i = 2; i < demandRouterArray.length; i++) {
                demandRouters.add(demandRouterArray[i]);
            }
            int randIndex = rand.nextInt(c1.pathUsedRouterId.size());
            ArrayList<Integer> databaseList = new ArrayList<Integer>();//存储路径中已使用的节点，目前是包含变异点在内及前驱节点
            for (int i = 0; i <= randIndex; i++) {
                databaseList.add(c1.pathUsedRouterId.get(i));
            }
            /****获得变异点前驱结点可以到达的节点，并且如果包含必须经过的节点，就随机选一个替换变异点*************************/
            try {
                intersectionList = new ArrayList<Integer>(neighbourTable.get(c1.pathUsedRouterId.get(randIndex - 1)));
            } catch (ArrayIndexOutOfBoundsException ex) {
                intersectionList = new ArrayList<Integer>(neighbourTable.get(demandRouterArray[0]));
            }
            intersectionList.retainAll(demandRouters);
            if (intersectionList.size() > 0) {
                for (int i = 0; i < 1000; i++) {
                    randIndex = rand.nextInt(intersectionList.size());
                    if (!databaseList.contains(intersectionList.get(randIndex))) {
                        c1.pathUsedRouterId.set(databaseList.size() - 1, intersectionList.get(randIndex));//使用一个必经节点替换变异点基因位值
                        databaseList.set(databaseList.size() - 1, intersectionList.get(randIndex));
                        randIndex = c1.pathUsedRouterId.size();//暂时存储路径总长度
                        for (int j = databaseList.size(); j < randIndex; j++) {
                            c1.pathUsedRouterId.remove(databaseList.size());//删除变异位之后的值
                        }
                        //databaseList.add(demandRouters.get())
                        break;
                    }
                }

            } else {//变异点的前驱结点并不经过任何一个必经点 intersectionList应该是空
                randIndex = c1.pathUsedRouterId.size();//暂时存储路径总长度
                for (int j = databaseList.size(); j < randIndex; j++) {
                    c1.pathUsedRouterId.remove(databaseList.size());//删除变异位之后的值
                }
            }

            /****获得变异点前驱结点可以到达的节点，并且如果包含必须经过的节点，就随机选一个替换变异点*************************/
            /*
            int c1Size=c1.pathUsedRouterId.size();
            for(int kk=randIndex;kk<c1Size-1;++kk){
                 c1.pathUsedRouterId.remove(randIndex+1);
            }
            */
            int breakCounter = -1;
            Integer tempValue = -1;
            while (!c1.pathUsedRouterId.get(c1.pathUsedRouterId.size() - 1).equals(demandRouterArray[1])) {
                randIndex = rand.nextInt(neighbourTable.get(c1.pathUsedRouterId.get(c1.pathUsedRouterId.size() - 1)).size());
                tempValue = neighbourTable.get(c1.pathUsedRouterId.get(c1.pathUsedRouterId.size() - 1)).get(randIndex);
                if (!databaseList.contains(tempValue)) {
                    c1.pathUsedRouterId.add(tempValue);
                    databaseList.add(tempValue);
                }
                breakCounter++;
                if (breakCounter > 300) {//变异后到终点没有路径，就保持原染色体不变，退出
                        c1.pathUsedRouterId = new ArrayList<Integer>(backupPath);

                        return -1; //变异失败，保持染色体不变，退出变异过程
                    //c1.pathUsedRouterId = (ArrayList)src2dstShorPath.clone();


                }
            }
            //int afterMutationIncludeRouters=0;
            /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
            c1.calTotalWeightAndIncludeRouters();
            /*
            c1.chromosomeFitness = (1 / (weightArguments[c1.includedRoutersInPath] *
                    c1.pathTotalWeight))*(Math.pow(2,c1.includedRoutersInPath+10));
            return 1;
              */
            if (c1.includedRoutersInPath<beforeMutationIncludeRouters-2){
                c1.pathUsedRouterId=new ArrayList<Integer>(backupPath);
                c1.chromosomeFitness=beforeMutationChrosomeFiteness;
                c1.pathTotalWeight=beforeMutationTotalWeight;
                c1.includedRoutersInPath=beforeMutationIncludeRouters;
                return -1;
            }
            else{
                c1.chromosomeFitness = (1 / (weightArguments[c1.includedRoutersInPath] *
                c1.pathTotalWeight))*(Math.pow(2,c1.includedRoutersInPath+10));
                return 1;

            }


            //成功找到一条新路径

            //return 1;//变异成功，保持染色体不变，退出变异过程
        }


        public int mutationMethod2(Chromosome c1){
            Random rand=new Random();
            ArrayList<Integer> backupPath = new ArrayList<Integer>(c1.pathUsedRouterId);
            ArrayList<Integer> tempDemandRouterList=new ArrayList<Integer>(demandRoutersList);
            ArrayList<Integer> tempC1List=c1.pathUsedRouterId;
            int c1Size=tempC1List.size();
            ArrayList<Integer> demandRouterIndexList=new ArrayList<Integer>();
            int lastDemandRouterIndex=-1;
            Integer tempValue;
            for (int i = 0; i <c1Size ; i++) {
                tempValue=tempC1List.get(i);
                if (demandRoutersList.contains(tempValue)){
                    demandRouterIndexList.add(tempValue);
                    lastDemandRouterIndex=i;
                }
            }
            if (lastDemandRouterIndex==-1){
                c1.chromosomeFitness=1e-5;
                return -1;
            }
            tempValue=tempC1List.get(lastDemandRouterIndex);
            //lastDemandRouterIndex=demandRouterIndexList.get(demandRouterIndexList.size()-1);
            for (int i = lastDemandRouterIndex+1; i <=c1Size-1 ; i++) {
                tempC1List.remove(lastDemandRouterIndex+1);
            }


            //是否可以加一个必经点，并且保证到这个必经点路径与之前路径不重合
            tempDemandRouterList.removeAll(demandRouterIndexList);//染色体路径中没有出现的点，这里是求差集
            tempDemandRouterList.retainAll(demandNextDemandRoutersMap.get(tempValue));//染色体最后一个必经点可以到达的必经点与路径中没有出现的点的交集
            ArrayList<Integer> copyOfTempC1List=new ArrayList<Integer>();
            StringBuilder sb=new StringBuilder();
            String sss;
            ArrayList<Integer> tt;
            for (int i = 0; i <tempDemandRouterList.size() ; i++) {
                copyOfTempC1List.addAll(tempC1List);
                sb.append(tempValue).append(",").append(tempDemandRouterList.get(i));
                sss=sb.toString();
                tt=demand2DemandPath.get(sss);
                if (!copyOfTempC1List.retainAll(tt)){
                    tempC1List.addAll(tt);
                    break;
                }
                else{
                    sb.delete(0,sb.length());
                }

            }
            //是否可以加一个必经点，并且保证到这个必经点路径与之前路径不重合
            c1Size=tempC1List.size();
            copyOfTempC1List.clear();
            tempValue=tempC1List.get(c1Size-1);
            int randIndex=-1;
            //int breakCount=0;
            ArrayList<Integer> copyOfTempNeighbour=new ArrayList<Integer>();
            while (!tempC1List.get(c1Size-1).equals(demandRouterArray[1])){
                copyOfTempNeighbour.addAll(neighbourTable.get(tempValue));
                copyOfTempC1List.addAll(tempC1List);
                copyOfTempC1List.retainAll(copyOfTempNeighbour);
                copyOfTempNeighbour.removeAll(copyOfTempC1List);
                if (copyOfTempNeighbour.size()>0){
                    randIndex=rand.nextInt(copyOfTempNeighbour.size());
                    tempValue=copyOfTempNeighbour.get(randIndex);
                    tempC1List.add(tempValue);
                    copyOfTempC1List.clear();
                    copyOfTempNeighbour.clear();
                }
                else{//不到其他点，保持原染色体不变
                    c1.pathUsedRouterId = new ArrayList<Integer>(backupPath);
                    return -1;
                }
                c1Size=tempC1List.size();
            }

            c1.calTotalWeightAndIncludeRouters();
            c1.chromosomeFitness = (1 / (weightArguments[c1.includedRoutersInPath] *
                    c1.pathTotalWeight))*(Math.pow(2,c1.includedRoutersInPath+10));
            return 1;
        }

        public void repairFunctionForCrossover(Chromosome c1) {
            int currentSize = -1;
            for (int i = 0; i < c1.pathUsedRouterId.size(); i++) {
                currentSize = c1.pathUsedRouterId.size();
                for (int j = currentSize - 1; j > i; j--) {
                    if (c1.pathUsedRouterId.get(j).equals(c1.pathUsedRouterId.get(i))) {
                        {//不能使用==来比较，因为c1中存储的是Integer，是引用，使用==实际上比较的是
                            for (int k = 0; k < j - i; k++) {                                //两个引用的地址值是否相等，并不是对象中的int型值是否相等
                                c1.pathUsedRouterId.remove(i + 1);
                            }
                            break;
                        }
                    }
                }
            }


        }
        public void repairFunction2(Chromosome c1){
            /********************************去除染色体中在起始点、必经点、以及终点之间的环*****************/
            ArrayList<Integer> demandRoutersIndex=new ArrayList<Integer>();
            Integer temp11;
            demandRoutersIndex.add(0);
            for (int i = 0; i <c1.pathUsedRouterId.size(); i++) {
                temp11=c1.pathUsedRouterId.get(i);
                if (demandRoutersList.contains(temp11)){
                    demandRoutersIndex.add(i);
                }
            }
            demandRoutersIndex.add(c1.pathUsedRouterId.size()-1);
            int startIndex,endIndex,onceDeleteRouters=0,cool=0;
            for (int i=0;i<demandRoutersIndex.size()-1;++i){
                startIndex=demandRoutersIndex.get(i)-onceDeleteRouters;
                endIndex=demandRoutersIndex.get(i+1)-onceDeleteRouters;
                //onceDeleteRouters=0;
                while (startIndex<endIndex) {
                    cool=0;
                    for (int i2 = endIndex; i2 > startIndex; i2--) {

                        if (c1.pathUsedRouterId.get(startIndex).equals(c1.pathUsedRouterId.get(i2))) {
                            onceDeleteRouters+=i2-startIndex;
                            cool=i2-startIndex;
                            for (int j = 0; j < i2 - startIndex; j++) {
                                c1.pathUsedRouterId.remove(startIndex + 1);

                            }
                            break;
                        }
                    }
                    startIndex++;
                    endIndex-=cool;
                }
            }


            /********************************去除染色体中在起始点、必经点、以及终点之间的环*****************/

        }

        public void updateWeightArguments(ArrayList<ArrayList<Integer>> arrayList, double[] weightArgument, Chromosome[] populationChromosome) {
            ArrayList<Integer> temp1List;//n
            ArrayList<Integer> temp2List;//n-1
            int nIndex = -1, n1Index = -1;
            int breakCounter=0;
            for (int i = demandRouterArray.length - 2; i > 0; i--) {
                temp1List = arrayList.get(i);
                temp2List = arrayList.get(i - 1);
                for (int j = 0; j < temp1List.size(); j++) {
                    nIndex = temp1List.get(j);
                    for (int k = 0; k < temp2List.size(); k++) {
                        n1Index = temp2List.get(k);
                        /***************满足EQ3的话我们就不更新了*****************************/
                        /*
                        if (populationChromosome[nIndex].pathTotalWeight*weightArgument[i]<
                                populationChromosome[n1Index].pathTotalWeight*weightArgument[i-1]){
                            weightArgument[i-1]=1.2*weightArgument[i-1];
                            continue;
                        }
                        */
                        /***************满足EQ3的话我们就不更新了*****************************

                        while((populationChromosome[nIndex].pathTotalWeight * weightArgument[i])>=
                                (populationChromosome[n1Index].pathTotalWeight * weightArgument[i - 1])) {
                            weightArgument[i - 1] = 1.2 * weightArgument[i - 1]<Double.MAX_VALUE?1.2* weightArgument[i - 1]:
                                    1.005*weightArgument[i - 1];
                            breakCounter++;
                            if (breakCounter>10){
                                break;
                            }
                        }
                        ***************满足EQ3的话我们就不更新了*****************************/

                    }
                }
            }


        }
        public void evolutionMethod2(double updateMutationprobability) throws CloneNotSupportedException {
            this.biggerMutationProbability=updateMutationprobability;
            Random rand = new Random();
            double totalFitness = 0;
            double parentbestChromosomeFiteness=parentChromosome[0].chromosomeFitness;
            int parentBestChromosomeIndex=0;
            //double[] selectionProbability = new double[popSize];
            ArrayList<Integer> tempParentList=new ArrayList<Integer>();
            for (int i = 0; i < popSize; i++) {
                //totalFitness += parentChromosome[i].chromosomeFitness;
                if (parentbestChromosomeFiteness< parentChromosome[i].chromosomeFitness) {
                    parentbestChromosomeFiteness = parentChromosome[i].chromosomeFitness;
                    parentBestChromosomeIndex = i;
                }
                tempParentList.add(i);

            }
            tempParentList.remove(parentBestChromosomeIndex);
            /*********************无放回锦标赛选择算子*****/
            int selectRandomIndex1,selectRandomIndex2,tournamentPopulation=2,betterFitnessIndex;
            for(int i=0;i<popSize/2;++i){
                selectRandomIndex1=rand.nextInt(tempParentList.size());
                selectRandomIndex2=rand.nextInt(tempParentList.size());
                while (selectRandomIndex1==selectRandomIndex2){
                    selectRandomIndex2=rand.nextInt(tempParentList.size());
                }
                selectRandomIndex1=tempParentList.get(selectRandomIndex1);
                selectRandomIndex2=tempParentList.get(selectRandomIndex2);
                betterFitnessIndex=parentChromosome[selectRandomIndex1].chromosomeFitness>parentChromosome[selectRandomIndex2].chromosomeFitness?
                        selectRandomIndex1:selectRandomIndex2;
                childChromosome[i]=(Chromosome)parentChromosome[betterFitnessIndex].clone();
                tempParentList.remove((Object)betterFitnessIndex);
                //tempParentList.remove((Object)selectRandomIndex2);
            }
            tempParentList.clear();
            for (int i = 0; i <popSize ; i++) {
                if (i!=parentBestChromosomeIndex){
                    tempParentList.add(i);
                }
            }
            //tempParentList.remove(parentBestChromosomeIndex) ;
            for(int i=popSize/2;i<popSize;++i){
                selectRandomIndex1=rand.nextInt(tempParentList.size());
                selectRandomIndex2=rand.nextInt(tempParentList.size());
                while (selectRandomIndex1==selectRandomIndex2){
                    selectRandomIndex2=rand.nextInt(tempParentList.size());
                }
                selectRandomIndex1=tempParentList.get(selectRandomIndex1);
                selectRandomIndex2=tempParentList.get(selectRandomIndex2);
                betterFitnessIndex=parentChromosome[selectRandomIndex1].chromosomeFitness>parentChromosome[selectRandomIndex2].chromosomeFitness?
                        selectRandomIndex1:selectRandomIndex2;
                childChromosome[i]=(Chromosome)parentChromosome[betterFitnessIndex].clone();
                tempParentList.remove((Object)betterFitnessIndex);
            }
           //*********************选择算子完成****************************************************************************/

            /*********************选择产生新种群之后，需要更新种群的总适应度、平均适应度以及选择概率，并更新此代精英***************************************/
             totalFitness=0;
             double tempBestChromosomeFiteness=childChromosome[0].chromosomeFitness;
             int tempBestChromosomeIndex=0;
             for (int i = 0; i < popSize; i++) {
                totalFitness += childChromosome[i].chromosomeFitness;
                if (tempBestChromosomeFiteness < childChromosome[i].chromosomeFitness) {
                    tempBestChromosomeFiteness = childChromosome[i].chromosomeFitness;
                    //tempBestChromosomeIndex = i;
             //}
             }
             }
             averageFitness = totalFitness / popSize;


             /*********************选择产生新种群之后，需要更新种群的总适应度、平均适应度以及选择概率，并更新此代精英***************************************/

            /*****************************************开始交叉算子********************************************************/
            double biggerFitness=-1, CrossoverProbability=-1,corssoverReturnvalue=0;
            ArrayList<Integer> remainIndividualIndex=new ArrayList<Integer>();
            for (int i=0;i<popSize;++i){
                remainIndividualIndex.add(i);
            }
            int c1IndividualIndex=-1,c2IndividualIndex=-1,matePairs=popSize/2;
            double tempProbability=0;
            for (int i=0;i<matePairs;++i){
                c1IndividualIndex=remainIndividualIndex.get(rand.nextInt(remainIndividualIndex.size()));
                remainIndividualIndex.remove((Object)c1IndividualIndex);
                c2IndividualIndex=remainIndividualIndex.get(rand.nextInt(remainIndividualIndex.size()));
                remainIndividualIndex.remove((Object)c2IndividualIndex);
                biggerFitness=childChromosome[c1IndividualIndex].chromosomeFitness>childChromosome[c2IndividualIndex].chromosomeFitness?
                        childChromosome[c1IndividualIndex].chromosomeFitness:childChromosome[c2IndividualIndex].chromosomeFitness;
                /***********************************************交叉开始******************************************/
                if (biggerFitness > averageFitness) {
                    tempProbability=(biggerCrossoverProbability-smallerCrossoverProbability)*(biggerFitness-averageFitness);
                    tempProbability=tempProbability/(tempBestChromosomeFiteness-averageFitness);
                    CrossoverProbability=biggerCrossoverProbability-tempProbability;
                } else {
                    CrossoverProbability = biggerCrossoverProbability;
                }
                //CrossoverProbability = biggerCrossoverProbability;//添加
                //System.out.println("CrossoverProbability= is:"+CrossoverProbability);
                if (rand.nextDouble() < CrossoverProbability) {//开始两点顺序交叉
                    corssoverReturnvalue = crossoverMethod(childChromosome[c1IndividualIndex], childChromosome[c2IndividualIndex], rand);
                    if (corssoverReturnvalue == 1) {//选出的两条染色体有共同的基因位点，可以顺利进行交叉，并且可以保证交叉之后的染色体是通路。否则，不能进行交叉
                        //i += 2;//表示交叉生成两条染色体成功
                        /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/

                         repairFunction2(childChromosome[c1IndividualIndex]);
                         repairFunction2(childChromosome[c2IndividualIndex]);
                        repairFunctionForCrossover(childChromosome[c1IndividualIndex]);
                        repairFunctionForCrossover(childChromosome[c2IndividualIndex]);

                        /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/
                        /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
                        childChromosome[c1IndividualIndex].calTotalWeightAndIncludeRouters();
                        childChromosome[c2IndividualIndex].calTotalWeightAndIncludeRouters();
                        childChromosome[c1IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c1IndividualIndex].includedRoutersInPath] *
                                childChromosome[c1IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c1IndividualIndex].includedRoutersInPath+10));
                        childChromosome[c2IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c2IndividualIndex].includedRoutersInPath] *
                                childChromosome[c2IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c2IndividualIndex].includedRoutersInPath+10));
                        /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/

                        //childChromosome[c1IndividualIndex]=(Chromosome)childChromosome[c1IndividualIndex].clone();
                        //childChromosome[c2IndividualIndex]=(Chromosome)childChromosome[c2IndividualIndex].clone();
                    } else {//不交叉的染色体也要去环。
                        /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/


                        repairFunction2(childChromosome[c1IndividualIndex]);
                        repairFunction2(childChromosome[c2IndividualIndex]);
                        repairFunctionForCrossover(childChromosome[c1IndividualIndex]);
                        repairFunctionForCrossover(childChromosome[c2IndividualIndex]);
                        /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/
                        /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
                        childChromosome[c1IndividualIndex].calTotalWeightAndIncludeRouters();
                        childChromosome[c2IndividualIndex].calTotalWeightAndIncludeRouters();
                        childChromosome[c1IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c1IndividualIndex].includedRoutersInPath] *
                                childChromosome[c1IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c1IndividualIndex].includedRoutersInPath+10));
                        childChromosome[c2IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c2IndividualIndex].includedRoutersInPath] *
                                childChromosome[c2IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c2IndividualIndex].includedRoutersInPath+10));
                        /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
                        //childChromosome[c1IndividualIndex]=(Chromosome)childChromosome[c1IndividualIndex].clone();
                        //childChromosome[c2IndividualIndex]=(Chromosome)childChromosome[c2IndividualIndex].clone();
                    }
                } else { //不交叉的染色体也要去环。
                    /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/

                    repairFunction2(childChromosome[c1IndividualIndex]);
                    repairFunction2(childChromosome[c2IndividualIndex]);
                   repairFunctionForCrossover(childChromosome[c1IndividualIndex]);
                   repairFunctionForCrossover(childChromosome[c2IndividualIndex]);


                    /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/
                    /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
                    childChromosome[c1IndividualIndex].calTotalWeightAndIncludeRouters();
                    childChromosome[c2IndividualIndex].calTotalWeightAndIncludeRouters();
                    childChromosome[c1IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c1IndividualIndex].includedRoutersInPath] *
                            childChromosome[c1IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c1IndividualIndex].includedRoutersInPath+10));
                    childChromosome[c2IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c2IndividualIndex].includedRoutersInPath] *
                            childChromosome[c2IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c2IndividualIndex].includedRoutersInPath+10));
                    /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
                    //childChromosome[c1IndividualIndex]=(Chromosome)parentChromosome[c1IndividualIndex].clone();
                    //childChromosome[c2IndividualIndex]=(Chromosome)parentChromosome[c2IndividualIndex].clone();
                }
            }


            /*****************************************交叉结束********************************************************/

            /********************交叉完成，产生新种群child之后，需要更新种群的总适应度、平均适应度,并更新此代精英***************************************/
            totalFitness=0;
            tempBestChromosomeFiteness=childChromosome[0].chromosomeFitness;
           // bestChromosomeIndex=0;
            for (int i = 0; i < popSize; i++) {
                totalFitness += childChromosome[i].chromosomeFitness;
              if (tempBestChromosomeFiteness < childChromosome[i].chromosomeFitness) {
                  tempBestChromosomeFiteness = childChromosome[i].chromosomeFitness;
                //    bestChromosomeIndex = i;
                }
            }
            averageFitness = totalFitness / popSize;
            //*********************交叉完成，产生新种群之后，需要更新种群的总适应度、平均适应度，并更新此代精英***************************************/

            /**********************************************开始变异**************************************************************/

            double MutionProbability=-1;
            //int mutationReturnValue=0;
            //ArrayList<Double> childChromosomeFitnessList=new ArrayList<Double>();
            for (int i=0;i<popSize;++i){
                  /**********************************交叉生成的两条染色体要不要变异**********************/
                  if (childChromosome[i].chromosomeFitness >= averageFitness) {
                      tempProbability=(biggerMutationProbability-smallerMutationProbability)*
                              (tempBestChromosomeFiteness-childChromosome[i].chromosomeFitness);
                      tempProbability=tempProbability/(tempBestChromosomeFiteness-averageFitness);
                      MutionProbability=biggerMutationProbability-tempProbability;
                      if (MutionProbability==Double.NEGATIVE_INFINITY||MutionProbability==Double.POSITIVE_INFINITY){
                          System.out.println("MutionProbability= is:"+MutionProbability);
                      }
                  } else {
                      MutionProbability = biggerMutationProbability;
                  }
                    //MutionProbability = biggerMutationProbability;
                //System.out.println("MutionProbability= is:"+MutionProbability);
                  if (rand.nextDouble() < MutionProbability) {
                      //mutationMethod(childChromosome[i], rand);//返回值1表示变异成功，-1表示变异失败保持原个体不变
                      mutationMethod2(childChromosome[i]);//返回值1表示变异成功，-1表示变异失败保持原个体不变
                      }
                //childChromosomeFitnessList.add(childChromosome[i].chromosomeFitness);
                  }

               /*****************************变异结束********************************************************************/
            //parentChromosome[popSize]=(Chromosome)childChromosome[popSize].clone();
            double worstFitness=childChromosome[0].chromosomeFitness;
            int worstFitnessIndex=0;
            int childSecondWorseIndex=1;
            double childSecondWorseFitness=childChromosome[1].chromosomeFitness;
            if (childChromosome[0].chromosomeFitness>childChromosome[1].chromosomeFitness){
                worstFitness=childChromosome[1].chromosomeFitness;
                worstFitnessIndex=1;
                childSecondWorseFitness=childChromosome[0].chromosomeFitness;
                childSecondWorseIndex=0;

            }
            //double bestFitness=parentChromosome[0].chromosomeFitness;
            // int bestFitnessIndex=0;
            double childBestChromosomeFitness=childChromosome[0].chromosomeFitness;
            int childBestChromosomeIndex=0;
            for (int i=0;i<popSize;++i){
                if (childChromosome[i].chromosomeFitness<worstFitness){
                    worstFitness=childChromosome[i].chromosomeFitness;
                    worstFitnessIndex=i;
                }
                else if (childChromosome[i].chromosomeFitness<childSecondWorseFitness){
                    childSecondWorseFitness=childChromosome[i].chromosomeFitness;
                    childSecondWorseIndex=i;
                }

                if (childChromosome[i].chromosomeFitness>childBestChromosomeFitness){
                    childBestChromosomeFitness=childChromosome[i].chromosomeFitness;
                    childBestChromosomeIndex=i;
                }
            }
            //Collections.sort(comparedWithWorstChromosome);
            //childSecondWorseIndex=comparedWithWorstChromosome.get(0);

            if (childBestChromosomeFitness<parentbestChromosomeFiteness){
                childChromosome[popSize]=(Chromosome)parentChromosome[parentBestChromosomeIndex].clone();
                childChromosome[worstFitnessIndex]=(Chromosome)parentChromosome[parentBestChromosomeIndex].clone();
                this.bestChromosomeIndex=worstFitnessIndex;
                this.worstChromosomeIndex=childSecondWorseIndex;

            }
            else {

                /*****************************************************保存最佳染色体,即精英  *******/
                childChromosome[popSize]=(Chromosome)childChromosome[childBestChromosomeIndex].clone();
                /*****************************************************保存最佳染色体,即精英  *******/
                this.bestChromosomeIndex=childBestChromosomeIndex;
                this.worstChromosomeIndex=worstFitnessIndex;
            }

            for (int i=0;i<popSize;++i){
                parentChromosome[i] = (Chromosome) childChromosome[i].clone();
            }
            parentChromosome[popSize]=(Chromosome)childChromosome[popSize].clone();
        }
        public void primaryPopulationEvolution() throws CloneNotSupportedException {

            Random rand = new Random();
            //double totalFitness = 0;
            ArrayList<Integer> tempParentList=new ArrayList<Integer>();
            for (int i = 0; i < popSize; i++) {
                tempParentList.add(i);
            }

            /*********************锦标赛选择算子*****/
            int selectRandomIndex1,selectRandomIndex2,tournamentPopulation=2,betterFitnessIndex;
            for(int i=0;i<popSize/2;++i){
                selectRandomIndex1=rand.nextInt(tempParentList.size());
                selectRandomIndex2=rand.nextInt(tempParentList.size());
                while (selectRandomIndex1==selectRandomIndex2){
                    selectRandomIndex2=rand.nextInt(tempParentList.size());
                }
                selectRandomIndex1=tempParentList.get(selectRandomIndex1);
                selectRandomIndex2=tempParentList.get(selectRandomIndex2);
                betterFitnessIndex=parentChromosome[selectRandomIndex1].chromosomeFitness>parentChromosome[selectRandomIndex2].chromosomeFitness?
                        selectRandomIndex1:selectRandomIndex2;
                childChromosome[i]=(Chromosome)parentChromosome[betterFitnessIndex].clone();
                tempParentList.remove((Object)selectRandomIndex1);
                tempParentList.remove((Object)selectRandomIndex2);
            }
            tempParentList.clear();
            for (int i = 0; i <popSize ; i++) {
                tempParentList.add(i);
            }
            for(int i=popSize/2;i<popSize;++i){
                selectRandomIndex1=rand.nextInt(tempParentList.size());
                selectRandomIndex2=rand.nextInt(tempParentList.size());
                while (selectRandomIndex1==selectRandomIndex2){
                    selectRandomIndex2=rand.nextInt(tempParentList.size());
                }
                selectRandomIndex1=tempParentList.get(selectRandomIndex1);
                selectRandomIndex2=tempParentList.get(selectRandomIndex2);
                betterFitnessIndex=parentChromosome[selectRandomIndex1].chromosomeFitness>parentChromosome[selectRandomIndex2].chromosomeFitness?
                        selectRandomIndex1:selectRandomIndex2;
                childChromosome[i]=(Chromosome)parentChromosome[betterFitnessIndex].clone();
                tempParentList.remove((Object)selectRandomIndex1);
                tempParentList.remove((Object) selectRandomIndex2);
            }

            //*********************选择算子完成****************************************************************************/


            /*****************************************开始交叉算子********************************************************/
            double biggerFitness=-1, CrossoverProbability=-1,corssoverReturnvalue=0;
            ArrayList<Integer> remainIndividualIndex=new ArrayList<Integer>();
            for (int i=0;i<popSize;++i){
                remainIndividualIndex.add(i);
            }
            int c1IndividualIndex=-1,c2IndividualIndex=-1,matePairs=popSize/2;
            double tempProbability;
            for (int i=0;i<matePairs;++i){
                c1IndividualIndex=remainIndividualIndex.get(rand.nextInt(remainIndividualIndex.size()));
                remainIndividualIndex.remove((Object)c1IndividualIndex);
                c2IndividualIndex=remainIndividualIndex.get(rand.nextInt(remainIndividualIndex.size()));
                remainIndividualIndex.remove((Object)c2IndividualIndex);
                //biggerFitness=childChromosome[c1IndividualIndex].chromosomeFitness>childChromosome[c2IndividualIndex].chromosomeFitness?
                        //childChromosome[c1IndividualIndex].chromosomeFitness:childChromosome[c2IndividualIndex].chromosomeFitness;
                /***********************************************交叉开始******************************************/
                CrossoverProbability=biggerCrossoverProbability;

                if (rand.nextDouble() < CrossoverProbability) {//开始两点顺序交叉
                    corssoverReturnvalue = crossoverMethod(childChromosome[c1IndividualIndex], childChromosome[c2IndividualIndex], rand);
                    if (corssoverReturnvalue == 1) {//选出的两条染色体有共同的基因位点，可以顺利进行交叉，并且可以保证交叉之后的染色体是通路。否则，不能进行交叉
                        //i += 2;//表示交叉生成两条染色体成功
                        /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/

                        repairFunction2(childChromosome[c1IndividualIndex]);
                        repairFunction2(childChromosome[c2IndividualIndex]);
                        repairFunctionForCrossover(childChromosome[c1IndividualIndex]);
                        repairFunctionForCrossover(childChromosome[c2IndividualIndex]);

                        /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/
                        /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
                        childChromosome[c1IndividualIndex].calTotalWeightAndIncludeRouters();
                        childChromosome[c2IndividualIndex].calTotalWeightAndIncludeRouters();
                        childChromosome[c1IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c1IndividualIndex].includedRoutersInPath] *
                                childChromosome[c1IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c1IndividualIndex].includedRoutersInPath+10));
                        childChromosome[c2IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c2IndividualIndex].includedRoutersInPath] *
                                childChromosome[c2IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c2IndividualIndex].includedRoutersInPath+10));
                        /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/

                        //childChromosome[c1IndividualIndex]=(Chromosome)childChromosome[c1IndividualIndex].clone();
                        //childChromosome[c2IndividualIndex]=(Chromosome)childChromosome[c2IndividualIndex].clone();
                    } else {//不交叉的染色体也要去环。
                        /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/


                        repairFunction2(childChromosome[c1IndividualIndex]);
                        repairFunction2(childChromosome[c2IndividualIndex]);
                        repairFunctionForCrossover(childChromosome[c1IndividualIndex]);
                        repairFunctionForCrossover(childChromosome[c2IndividualIndex]);
                        /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/
                        /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
                        childChromosome[c1IndividualIndex].calTotalWeightAndIncludeRouters();
                        childChromosome[c2IndividualIndex].calTotalWeightAndIncludeRouters();
                        childChromosome[c1IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c1IndividualIndex].includedRoutersInPath] *
                                childChromosome[c1IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c1IndividualIndex].includedRoutersInPath+10));
                        childChromosome[c2IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c2IndividualIndex].includedRoutersInPath] *
                                childChromosome[c2IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c2IndividualIndex].includedRoutersInPath+10));
                        /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
                        //childChromosome[c1IndividualIndex]=(Chromosome)childChromosome[c1IndividualIndex].clone();
                        //childChromosome[c2IndividualIndex]=(Chromosome)childChromosome[c2IndividualIndex].clone();
                    }
                } else { //不交叉的染色体也要去环。
                    /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/

                    repairFunction2(childChromosome[c1IndividualIndex]);
                    repairFunction2(childChromosome[c2IndividualIndex]);
                    repairFunctionForCrossover(childChromosome[c1IndividualIndex]);
                    repairFunctionForCrossover(childChromosome[c2IndividualIndex]);


                    /********************使用修复函数修复交叉后染色体中可能出现的环**************************************************/
                    /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
                    childChromosome[c1IndividualIndex].calTotalWeightAndIncludeRouters();
                    childChromosome[c2IndividualIndex].calTotalWeightAndIncludeRouters();
                    childChromosome[c1IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c1IndividualIndex].includedRoutersInPath] *
                            childChromosome[c1IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c1IndividualIndex].includedRoutersInPath+10));
                    childChromosome[c2IndividualIndex].chromosomeFitness = (1 / (weightArguments[childChromosome[c2IndividualIndex].includedRoutersInPath] *
                            childChromosome[c2IndividualIndex].pathTotalWeight))*(Math.pow(2,childChromosome[c2IndividualIndex].includedRoutersInPath+10));
                    /**********************************更新染色体，包括新路径总权重、包含的必经节点数量以及适应度值**********************/
                    //childChromosome[c1IndividualIndex]=(Chromosome)parentChromosome[c1IndividualIndex].clone();
                    //childChromosome[c2IndividualIndex]=(Chromosome)parentChromosome[c2IndividualIndex].clone();
                }
            }


            /*****************************************交叉结束********************************************************/

            /*****************************************以上完成了新种群的生成，接下来更新新种群的权值参数***********************************/
            ArrayList<ArrayList<Integer>> groups2 = new ArrayList<ArrayList<Integer>>();
            for (int i = 0; i < demandRouterArray.length - 1; i++) {
                groups2.add(new ArrayList<Integer>());
                //groups[i]=new ArrayList<Integer>();//经过i个必经节点的染色体的在population中的索引存在group中
            }
            for (int i = 0; i < popSize; i++) {
                // parentChromosome[i] = new Chromosome(weightArgumentMap);
                for (int j = 0; j < demandRouterArray.length - 1; j++) {
                    if (childChromosome[i].includedRoutersInPath == j) {
                        groups2.get(j).add(i);
                        break;
                    }
                }
            }
            //updateWeightArguments(groups2, weightArguments, childChromosome);
            primaryUpdateWeightArguments(groups2, weightArguments, childChromosome);
            /*****************************************完成了更新新种群的权值参数***********************************/

            /****根据更新过的权值参数更新染色体的适应度值***********************/
            for (int i = 0; i < popSize; i++) {
                childChromosome[i].chromosomeFitness = (1 / (weightArguments[childChromosome[i].includedRoutersInPath] *
                        childChromosome[i].pathTotalWeight))*(Math.pow(2,childChromosome[i].includedRoutersInPath+10));
            }

            //*********************交叉完成，产生新种群之后，需要更新种群的总适应度、平均适应度，并更新此代精英***************************************/

            /**********************************************开始变异**************************************************************/

            double MutionProbability=-1;
            for (int i=0;i<popSize;++i){
                /**********************************交叉生成的两条染色体要不要变异**********************/

                MutionProbability = smallerMutationProbability;

                if (rand.nextDouble() < MutionProbability) {
                   //mutationMethod(childChromosome[i], rand);//返回值1表示变异成功，-1表示变异失败保持原个体不变
                    mutationMethod2(childChromosome[i]);
                }
            }
            /*****************************变异结束********************************************************************/
            /********************变异完成，产生新种群child之后，只需要并更新此代精英***************************************/

            /*****************************************以上完成了新种群的生成，接下来更新新种群的权值参数***********************************/
            ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>();
            for (int i = 0; i < demandRouterArray.length - 1; i++) {
                groups.add(new ArrayList<Integer>());
                //groups[i]=new ArrayList<Integer>();//经过i个必经节点的染色体的在population中的索引存在group中
            }
            for (int i = 0; i < popSize; i++) {
                // parentChromosome[i] = new Chromosome(weightArgumentMap);
                for (int j = 0; j < demandRouterArray.length - 1; j++) {
                    if (childChromosome[i].includedRoutersInPath == j) {
                        groups.get(j).add(i);
                        break;
                    }
                }
            }
            //updateWeightArguments(groups, weightArguments, childChromosome);
            primaryUpdateWeightArguments(groups,weightArguments,childChromosome);
            /*****************************************完成了更新新种群的权值参数***********************************/

            /****接下来就是将新染色体复制到父代染色体,不过首先要根据更新过的权值参数更新染色体的适应度值***********************/
            //childChromosome[popSize] = (Chromosome) parentChromosome[popSize].clone();
            for (int i = 0; i < popSize; i++) {
                childChromosome[i].calTotalWeightAndIncludeRouters();
                childChromosome[i].chromosomeFitness = (1 / (weightArguments[childChromosome[i].includedRoutersInPath] *
                        childChromosome[i].pathTotalWeight))*(Math.pow(2,childChromosome[i].includedRoutersInPath+10));
                parentChromosome[i]=(Chromosome)childChromosome[i].clone();

            }
            //parentChromosome[popSize]=(Chromosome)childChromosome[popSize].clone();
            //double worstFitness=childChromosome[0].chromosomeFitness;
            //int worstFitnessIndex=0;
            //double bestFitness=parentChromosome[0].chromosomeFitness;
            // int bestFitnessIndex=0;
            double childBestChromosomeFitness=childChromosome[0].chromosomeFitness;
            int childBestChromosomeIndex=0;
            for (int i=1;i<popSize;++i){
                if (childChromosome[i].chromosomeFitness>childBestChromosomeFitness){
                    childBestChromosomeFitness=childChromosome[i].chromosomeFitness;
                    childBestChromosomeIndex=i;
                }
            }
            this.bestChromosomeIndex= childBestChromosomeIndex;
            parentChromosome[popSize]=(Chromosome)parentChromosome[bestChromosomeIndex].clone();

            /***********************上代精英替换本代最差个体*******************************************************
             if(childChromosome[popSize].chromosomeFitness<parentChromosome[popSize].chromosomeFitness){
             parentChromosome[worstFitnessIndex]=(Chromosome)parentChromosome[popSize].clone();
             }else{
             parentChromosome[worstFitnessIndex]=(Chromosome)childChromosome[popSize].clone();
             }

             parentChromosome[popSize]=(Chromosome)childChromosome[popSize].clone();*/

             }
        public void primaryUpdateWeightArguments(ArrayList<ArrayList<Integer>> arrayList, double[] weightArgument, Chromosome[] populationChromosome) {
            ArrayList<Integer> temp1List;//n
            ArrayList<Integer> temp2List;//n-1
            int nIndex = -1, n1Index = -1;
            int breakCounter=0;
            for (int i = demandRouterArray.length - 2; i > 0; i--) {
                temp1List = arrayList.get(i);
                temp2List = arrayList.get(i - 1);
                for (int j = 0; j < temp1List.size(); j++) {
                    nIndex = temp1List.get(j);
                    for (int k = 0; k < temp2List.size(); k++) {
                        n1Index = temp2List.get(k);
                        /***************满足EQ3的话我们就不更新了*****************************/
                        /*
                        if (populationChromosome[nIndex].pathTotalWeight*weightArgument[i]<
                                populationChromosome[n1Index].pathTotalWeight*weightArgument[i-1]){
                            weightArgument[i-1]=1.2*weightArgument[i-1];
                            continue;
                        }
                        */
                        /***************满足EQ3的话我们就不更新了*****************************/

                         while((populationChromosome[nIndex].pathTotalWeight * weightArgument[i])>=
                         (populationChromosome[n1Index].pathTotalWeight * weightArgument[i - 1])) {
                         weightArgument[i - 1] = 1.2 * weightArgument[i - 1]<Double.MAX_VALUE?1.2* weightArgument[i - 1]:
                         1.005*weightArgument[i - 1];
                         breakCounter++;
                         if (breakCounter>100){
                         break;
                         }
                         }
                         /***************满足EQ3的话我们就不更新了*****************************/

                    }
                }
            }


        }
    }
}












