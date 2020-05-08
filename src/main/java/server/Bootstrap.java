package server;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.tools.reflect.Loader;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Minicat的主类
 */
public class Bootstrap {

    ConfigPort configPort = ConfigPort.getInstance();

    /**
     * Minicat启动需要初始化展开的一些操作
     */
    public void start() throws Exception {

        // 加载解析服务器配置信息，server.xml
        lodServerInfo();

        // 获取部署在服务器上的所有文件
        for (HostInfo hostInfo : hostInfoList) {
            // 存放获取到的所有项目根路径
            List<String> projectPathList = new ArrayList<String>();
            getFileInWebapps(hostInfo,projectPathList);
            // 加载解析相关的配置，web.xml
            for (String projectPath : projectPathList) {
                loadServlet(projectPath);
            }

        }





        // 定义一个线程池
        int corePoolSize = 10;
        int maximumPoolSize =50;
        long keepAliveTime = 100L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler
        );





        /*
            完成Minicat 1.0版本
            需求：浏览器请求http://localhost:8080,返回一个固定的字符串到页面"Hello Minicat!"
         */
        /*定义socket监听的端口号*/
        // 获取配置文件中的端口号
        int port = configPort.getPort();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("=====>>>Minicat start on port：" + port);

        /*while(true) {
            Socket socket = serverSocket.accept();
            // 有了socket，接收到请求，获取输出流
            OutputStream outputStream = socket.getOutputStream();
            String data = "Hello Minicat!";
            String responseText = HttpProtocolUtil.getHttpHeader200(data.getBytes().length) + data;
            outputStream.write(responseText.getBytes());
            socket.close();
        }*/


        /**
         * 完成Minicat 2.0版本
         * 需求：封装Request和Response对象，返回html静态资源文件
         */
        /*while(true) {
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();

            // 封装Request对象和Response对象
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());

            response.outputHtml(request.getUrl());
            socket.close();

        }*/


        /**
         * 完成Minicat 3.0版本
         * 需求：可以请求动态资源（Servlet）
         */
        /*while(true) {
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();

            // 封装Request对象和Response对象
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());

            // 静态资源处理
            if(servletMap.get(request.getUrl()) == null) {
                response.outputHtml(request.getUrl());
            }else{
                // 动态资源servlet请求
                HttpServlet httpServlet = servletMap.get(request.getUrl());
                httpServlet.service(request,response);
            }

            socket.close();

        }
*/

        /*
            多线程改造（不使用线程池）
         */
        /*while(true) {
            Socket socket = serverSocket.accept();
            RequestProcessor requestProcessor = new RequestProcessor(socket,servletMap);
            requestProcessor.start();
        }*/



        System.out.println("=========>>>>>>使用线程池进行多线程改造");
        /*
            多线程改造（使用线程池）
         */
        while(true) {

            Socket socket = serverSocket.accept();
            RequestProcessor requestProcessor = new RequestProcessor(socket,servletMap);
            //requestProcessor.start();
            threadPoolExecutor.execute(requestProcessor);
        }



    }



    // 获取配置在webapps中为所有文件
    private void getFileInWebapps(HostInfo hostInfo,List<String> projectPathList) {
        System.out.println("hostPath = " + hostInfo.getAppBase());
        String appBase = hostInfo.getAppBase();
        // 输入静态资源文件
        File file = new File(appBase);
        File[] listFiles = file.listFiles();
        for (File listFile : listFiles) {
            String fileName = listFile.getName();
            System.out.println("=======>listFile name= " + listFile.getName());
            if(!fileName.startsWith(".")){
                String projectPath = appBase+"/"+fileName;
                projectPathList.add(projectPath);
            }
        }
    }

    // 存放配置文件server.xml中的Host信息
    private List<HostInfo> hostInfoList = new ArrayList<HostInfo>();

    /**
     * 加载解析server.xml，获取服务器相关配置信息
     */
    private void lodServerInfo() {
        InputStream serveresourceAsStream = this.getClass().getClassLoader().getResourceAsStream("server.xml");
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(serveresourceAsStream);
            Element rootElement = document.getRootElement();
            List<Element> selectNodes = rootElement.selectNodes("//Service");
            for (Element selectNode : selectNodes) {
                Element connector = (Element) selectNode.selectSingleNode("Connector");
                String port = connector.attributeValue("port");
                configPort.setPort(Integer.valueOf(port));
                System.out.println("=====>"+port);
                List<Element> engineSelectNodes = rootElement.selectNodes("//Engine");
                for (Element engineSelectNode : engineSelectNodes) {
                    Element hostElementInfo = (Element) engineSelectNode.selectSingleNode("Host");
                    String hostName = hostElementInfo.attributeValue("name");
                    String appBase = hostElementInfo.attributeValue("appBase");
                    HostInfo hostInfo = new HostInfo(hostName,appBase);
                    hostInfoList.add(hostInfo);
                    System.out.println("=====>"+hostName+"======="+appBase);
                }

            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    //  用来存放配置文件web.xml中配置的servlet信息
    private Map<String,HttpServlet> servletMap = new HashMap<String,HttpServlet>();

    /**
     * 加载解析web.xml，初始化Servlet
     */
    private void loadServlet(String projectPath) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(projectPath+"/web.xml");
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(fileInputStream);
            Element rootElement = document.getRootElement();

            List<Element> selectNodes = rootElement.selectNodes("//servlet");
            for (int i = 0; i < selectNodes.size(); i++) {
                Element element =  selectNodes.get(i);
                // <servlet-name>lagou</servlet-name>
                Element servletnameElement = (Element) element.selectSingleNode("servlet-name");
                String servletName = servletnameElement.getStringValue();
                // <servlet-class>server.LagouServlet</servlet-class>
                Element servletclassElement = (Element) element.selectSingleNode("servlet-class");
                String servletClass = servletclassElement.getStringValue();
                String servletClass1 = projectPath.replaceAll("/",".")+"."+servletClass;
                System.out.println("servletClass = " + servletClass);
                String[] split = servletClass.split("\\.");
                File f = new File(projectPath+"/"+split[0]+"/"+split[1]);
                System.out.println("f = " + f.exists());

                // 根据servlet-name的值找到url-pattern
                Element servletMapping = (Element) rootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");
                // /lagou
                String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();
                System.out.println("urlPattern = " + urlPattern);
                servletMap.put(urlPattern, (HttpServlet) Class.forName(servletClass1).newInstance());

            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * Minicat 的程序启动入口
     * @param args
     */
    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        try {
            // 启动Minicat
            bootstrap.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
