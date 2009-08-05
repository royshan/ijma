/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jma;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 *
 * @author Vernkin
 */
public class JMAXmlServlet extends HttpServlet {

    private void toClient(PrintWriter pw, String msg) {
        pw.print(msg);
        pw.flush();
        pw.close();
    }

    /**
     * Return the xml with specific id.
     * If not found, return String "Not Exist Such ID : " + id;
     * @param id
     * @return
     */
    private String readXml(String id) {
        //System.out.println(new java.util.Date());
        //System.out.println("id: " + id);
        // read from diff xml file
        File dest = new File(StatisticManager.DB_PATH + id + "/" + StatisticManager.DIFF_FILE_NAME);
        if(!dest.exists()){
            return "Not Exist Such ID : " + id;
        }

        InputStream is = null;
        try {
            byte[] buf = new byte[(int)dest.length()];
            is = new FileInputStream(dest);
            is.read(buf);
            //System.out.println(new String(buf));
            return new String(buf);
        } catch (Exception e) {
            return "Error in read xml :" + e.getMessage();
        } finally{
            if(is != null){
                try{is.close();}catch(Exception ee){}
            }
        }
        
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String id = request.getParameter("id");
        // request the xml with specific id
        if (id != null) {
            String xmlStr = readXml(id);
            toClient(response.getWriter(), xmlStr);
            return;
        }

        ServletInputStream is = request.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(6120);
        byte[] buf = new byte[1024];
        try {
            int readLen = is.read(buf);
            while (readLen > 0) {
                bos.write(buf, 0, readLen);
                readLen = is.read(buf);
            }
        } catch (Throwable ex) {
            toClient(response.getWriter(), "Error in read input stream : " + ex.getMessage());
            return;
        }
        //use default utf8 encoding
        String xmlStr = bos.toString();
        //System.out.println("Write Xml "+xmlStr);

        Document doc = null;

        try {
            doc = DocumentHelper.parseText(xmlStr);
        } catch (Throwable ex) {
            toClient(response.getWriter(), "Error in parse xml : " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        id = null;
        String name = null;
        int upTotal = 0;
        int downTotal = 0;
        int sameTotal = 0;
        int sameError = 0;
        int upDiffError = 0;
        int downDiffError = 0;

        try {
            Element statEle = doc.getRootElement().element("stat");
            id = statEle.elementText("id");
            name = statEle.elementText("name");
            upTotal = Integer.parseInt(statEle.elementText("upTotal"));
            downTotal = Integer.parseInt(statEle.elementText("downTotal"));
            sameTotal = Integer.parseInt(statEle.elementText("sameTotal"));
            sameError = Integer.parseInt(statEle.elementText("sameError"));
            upDiffError = Integer.parseInt(statEle.elementText("upDiffError"));
            downDiffError = Integer.parseInt(statEle.elementText("downDiffError"));
        } catch (Throwable ex) {
            toClient(response.getWriter(), "Error in analysis xml : " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        try {
            // save to comparison file
            PrintWriter pw = new PrintWriter(new FileWriter(StatisticManager.DB_PATH + id + "/" + StatisticManager.DIFF_FILE_NAME));
            pw.print(xmlStr);
            pw.close();
        } catch (Throwable ex) {
            toClient(response.getWriter(), "Error in saving xml : " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet 方法。单击左侧的 + 号以编辑代码。">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}