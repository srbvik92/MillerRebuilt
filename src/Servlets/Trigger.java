package Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import objects.RequestHandler;
import services.LoginService;
import services.TriggerService;

/**
 * Servlet implementation class Trigger
 */
@WebServlet(description = "This servlet handles requests for Project Triggers", urlPatterns = { "/Trigger" })
//@WebServlet("Trigger")
public class Trigger extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	PrintWriter out;
       
    public Trigger() 
    {
        super();
    }

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		if(!LoginService.verify(req)) { resp.setContentType("plain/text"); out = resp.getWriter(); out.println("VERIFICATION_FAILURE"); return;}
		resp.setContentType("application/json");
		out = resp.getWriter();
		String response = "";
		Map<String, String> parameters = RequestHandler.getParameters((req.getParameterMap()));
		String action = parameters.get("action");
				
		if (action.equals("getTriggers")) {
			TriggerService ts = new TriggerService();
			response = ts.getAllTriggersAsJson();
		} else if(action.equals("getProjectTriggers")) {
			TriggerService ts = new TriggerService(Long.parseLong(req.getParameter("project_id")));
			response = ts.getAllSpecificTriggersAsJson();
		} else if(action.equals("submitTrigger")) {
			System.out.println("submitTrigger");
			
			JsonParser parser = new JsonParser();
			// TODO parameters.get("parameters") will yield a list of JSON object(s), not a single JSON object
			JsonObject o = null;
			if(!parameters.get("parameters").isEmpty() && parameters.get("parameters") != null)
				o = parser.parse(parameters.get("parameters")).getAsJsonObject();
			System.out.println(o);

			if(o == null) {
				response = "Something has gone horribly wrong";
			} else {
				String[] params = {"cost=43"};
				//String[] params = {/*"cost = 0", "mcsNumber=-1"*/"CURDATE() between DATE_SUB(scheduledTurnover,INTERVAL 5 DAY) and DATE_SUB(scheduledTurnover,INTERVAL 0 DAY)"};
				projectObjects.Trigger trigger = new projectObjects.Trigger(projectObjects.Project.class, "Cost is zero!", 2, params);
				trigger.runTrigger();

				
				response = "submittedTrigger";
			}
		}
		
		System.out.println("Response: " + response);
		out.print(response);
	}

}
