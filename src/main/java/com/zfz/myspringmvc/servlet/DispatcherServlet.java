package com.zfz.myspringmvc.servlet;


import com.zfz.myspringmvc.annotation.*;
import com.zfz.myspringmvc.controller.UserController;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Created by zl on 2018-08-11.
 */
@WebServlet(name = "dispatchServlet",urlPatterns = "/*",loadOnStartup = 1,
initParams = {@WebInitParam(name = "base-package",value = "com.zfz.myspringmvc")})
public class DispatcherServlet extends HttpServlet {
    //扫描的基包
    private String basePackage = "";
    //基包下面所有的带包路径权限定类名
    private List<String> packageNames = new ArrayList<String>();
    //注解实例化，注解上的名称：实例化对象
    private Map<String,Object> instanceMap = new HashMap<String,Object>();
    //带包路径的权限定名称：注解上的名称
    private Map<String,String>nameMap = new HashMap<String, String>();
    //url地址和方法的映射关系 SpringMvc就是方法调用链
    private Map<String,Method>urlMethodMap =new HashMap<String, Method>();
    //Method和全限定类名映射关系 主要是为了通过Method找到该方法的对象利用反射执行
    private Map<Method,String>methodPackageMap =new HashMap<Method, String>();
    @Override
    public void init(ServletConfig config){
        basePackage = config.getInitParameter("base-packeage");

        try {
            //1.扫描基包得到全部的带包权限定名
            scanBasePackage(basePackage);
            //2.把带有@controller/@Service/@Repository的类实例化放入MAP中,key为注解上的名称
            instance(packageNames);
            //3.SpringIOC注入
            springIOC();
            //4.完成URL地址与方法的映射关系
            handlerUrlMethodMap();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    private void scanBasePackage(String basePackage){
        URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.","/"));
        File basePackageFile = new File(url.getPath());
        System.out.println("scan..."+basePackageFile);
        File[] childFiles = basePackageFile.listFiles();
        for(File file:childFiles){
            if(file.isDirectory()){
                scanBasePackage(basePackage+"."+file.getName());
            }else if(file.isFile()){
                //for example userController.class--- drop .class
                packageNames.add(basePackage+"."+file.getName().split("\\.")[0]);
            }
        }
    }

    private void instance(List<String>packageNames) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(packageNames.size()<1)
            return;
        for(String string :packageNames){
            Class c = Class.forName(string);
            if(c.isAnnotationPresent(Controller.class)){
                Controller controler = (Controller)c.getAnnotation(Controller.class);
                String controllerName = controler.value();
                instanceMap.put(controllerName,c.newInstance());
                System.out.println("Controller : "+string+" ,value :"+controllerName);
            }else if(c.isAnnotationPresent(Service.class)){
                Service service = (Service) c.getAnnotation(Service.class);
                String servicerName = service.value();
                instanceMap.put(servicerName,c.newInstance());
                System.out.println("Servive : "+string+" ,value :"+servicerName);
            }else if(c.isAnnotationPresent(Repository.class)){
                Repository repository = (Repository) c.getAnnotation(Repository.class);
                String repositoryName = repository.value();
                instanceMap.put(repositoryName,c.newInstance());
                System.out.println("Servive : "+string+" ,value :"+repositoryName);
            }
        }
    }


    private void springIOC() throws IllegalAccessException {
        for(Map.Entry<String,Object>entry:instanceMap.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for(Field field:fields){
                if(field.isAnnotationPresent(Qualifier.class)){
                    String name = field.getAnnotation(Qualifier.class).value();
                    field.setAccessible(true);
                    field.set(entry.getValue(),instanceMap.get(name));
                }
            }
        }
    }

    private void handlerUrlMethodMap() throws ClassNotFoundException {
        if(packageNames.size()<1)
            return;
        for(String string:packageNames){
            Class c = Class.forName(string);
            if(c.isAnnotationPresent(Controller.class)){
                Method[] methods = c.getMethods();
                StringBuffer baseUrl = new StringBuffer();
                if(c.isAnnotationPresent(RequestMapping.class)){
                    RequestMapping requestMapping = (RequestMapping) c.getAnnotation(RequestMapping.class);
                    baseUrl.append(requestMapping.value());
                }
                for(Method method :methods){
                    if(method.isAnnotationPresent(RequestMapping.class)){
                        RequestMapping requestMapping = (RequestMapping)method.getAnnotation(RequestMapping.class);
                        baseUrl.append(requestMapping.value());
                        urlMethodMap.put(baseUrl.toString(),method);
                        methodPackageMap.put(method,string);
                    }
                }
            }
        }
    }
    @Override
    protected void doGet(HttpServletRequest req,HttpServletResponse resp){
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp){
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = url.replaceAll(contextPath,"");
        //通过path找到Method方法
        Method method = urlMethodMap.get(path);
        if(method!=null){
            //通过Method拿到Controller对象，准备反射
            String packageName = methodPackageMap.get(method);
            String controllerName = nameMap.get(packageName);
            //拿到controller对象
            UserController userController = (UserController) instanceMap.get(controllerName);
            method.setAccessible(true);
            try {
                method.invoke(userController);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) throws ServletException {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.scanBasePackage("com.zfz.myspringmvc");
    }


}
