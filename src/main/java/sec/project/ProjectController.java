package sec.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ProjectController {
    
    private Map<String, String> session;
    
    public ProjectController(){
        session = new HashMap<>();
        Connection connection;
        try {
            connection = DriverManager.getConnection("jdbc:h2:file:./database", "sa", "");
        } catch (SQLException ex) {
            return;
        }
        try {
            connection.createStatement().executeUpdate("CREATE TABLE Account (\n"
                    + "    name varchar(20) PRIMARY KEY,\n"
                    + "    password varchar(20));");
        } catch (SQLException ex) {}
        try {
            connection.createStatement().executeUpdate("CREATE TABLE Message (\n"
                    + "    subject varchar(30) PRIMARY KEY,\n"
                    + "    message varchar(200));");
        } catch (SQLException ex) {}
    }
    
    @RequestMapping(value = "*")
    public String handleDefault(){
        return "redirect:/login";
    }

    @RequestMapping(value = "/account")
    @ResponseBody
    public String loadAccount(@RequestParam String name) {
        String toReturn = "";
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:file:./database", "sa", "");
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Account");
            while(resultSet.next()){
                if(name.equals(resultSet.getString("name"))){
                    toReturn += "Username: " + resultSet.getString("name") + "<br>";
                    toReturn += "Password: " + resultSet.getString("password") + "<br><br>";
                }
            }
        } catch (SQLException ex) {
            toReturn = "Account not found.<br>";
        }
        toReturn += "<a href=\"main\">Back</a>";
        return toReturn;
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loadLogin() {
        return "login";
    }
    
    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String loadSignup() {
        return "signup";
    }
    
    @RequestMapping(value = "/writemessage", method = RequestMethod.GET)
    public String loadWriteMessage(){
        return "writemessage";
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String submitLogin(HttpSession http, @RequestParam String name, @RequestParam String password) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:file:./database", "sa", "");
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Account");
            while(resultSet.next()){
                if(name.equals(resultSet.getString("name")) && password.equals(resultSet.getString("password"))){
                    if(session.containsKey(http.getId())){
                        session.replace(http.getId(), name);
                    }else{
                        session.put(http.getId(), name);
                    }
                    return "redirect:/main";
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "redirect:/login";
    }
    
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String submitSignup(@RequestParam String name, @RequestParam String password) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:file:./database", "sa", "");
            connection.createStatement().executeUpdate("INSERT INTO Account(name, password) VALUES ('" + name + "', '" + password + "');");
        } catch (SQLException ex) {
            return "redirect:/signup";
        }
        return "redirect:/login";
    }
    
    @RequestMapping(value ="/writemessage", method = RequestMethod.POST)
    public String submitWriteMessage(@RequestParam String subject, @RequestParam String message){
        if(subject == null || subject.isEmpty()){
            return "redirect:/writemessage";
        }
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:file:./database", "sa", "");
            connection.createStatement().executeUpdate("INSERT INTO Message(subject, message) VALUES ('" + subject + "', '" + message + "');");
        } catch (SQLException ex) {}
        return "redirect:/main";
    }
    
    @RequestMapping(value = "/main")
    @ResponseBody
    public String loadMain(HttpSession http){
        String toReturn = "";
        if(session.get(http.getId()) == null){
            toReturn += "Not logged in. <a href=\"login\">Log  in</a>";
        }else{
            toReturn += "Logged in as " + session.get(http.getId()) + ".<br>";
            toReturn += "<a href=\"account?name=" + session.get(http.getId()) + "\">View account</a>";
        }
        toReturn += "<br><br>";
        toReturn += "<a href=\"writemessage\">Write message</a>";
        toReturn += "<br><br><br>";
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:file:./database", "sa", "");
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Message");
            toReturn += "Messages:<br><br>";
            while(resultSet.next()){
                toReturn += "<a href=\"message?subject=" + resultSet.getString("subject") + "\">Subject: " + resultSet.getString("subject") + "</a><br><br>";
            }
        } catch (SQLException ex) {
            toReturn += "No messages.";
        }
        return toReturn;
    }
    
    @RequestMapping(value = "/message")
    @ResponseBody
    public String loadMessage(@RequestParam String subject){
        String message = "<a href=\"main\">Back</a><br><br>";
            Connection connection;
        try {
            connection = DriverManager.getConnection("jdbc:h2:file:./database", "sa", "");
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Message");
            while(resultSet.next()){
                if(resultSet.getString("subject").equals(subject)){
                    message += "Subject: " + subject + "<br><br>"
                            + "Message: " + resultSet.getString("message");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return message;
    }
}