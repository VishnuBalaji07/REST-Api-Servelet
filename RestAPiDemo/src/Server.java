import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Server {
	private static Connection getDatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/demo", "root", "Vishnu@tj");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
	

	
	
		public static void create(HttpExchange exchange) throws IOException, SQLException {
			try {
		String sql="insert into students(id,name,age)VALUES(?,?,?)";
	    String requestBody = new String(exchange.getRequestBody().readAllBytes());
	    JsonObject jsonObject =  JsonParser.parseString(requestBody).getAsJsonObject();
	    int id = jsonObject.get("id").getAsInt();
	    String name =jsonObject.get("name").getAsString();
	    int age=jsonObject.get("age").getAsInt();
	    
	    
//	    JsonObject jsonObject = JsonParser.parseString(requestBody);
	    Connection con =getDatabaseConnection();
	    PreparedStatement stmt=con.prepareStatement(sql);
	    
	   
	    stmt.setInt(1, id);
	    stmt.setString(2, name);
        stmt.setInt(3, age);
	    

	    int rowsAffected = stmt.executeUpdate();
	    
	    if(rowsAffected>0) {
        	JsonObject responseJson = jsonObject;
        	 sendJsonResponse(exchange, 200, responseJson);
        	
        }else {
        	handleError(exchange, 404, "Failed to insert student.");
        
        }
	    
	    }catch (SQLException e) {
	    	handleError(exchange, 400,e.getMessage());
	    	
	    }
	    
			
			
	}
	
		private static void sendJsonResponse(HttpExchange exchange, int statusCode, JsonObject responseJson) throws IOException {
	        byte[] responseBytes = responseJson.toString().getBytes();
	        exchange.getResponseHeaders().set("Content-Type", "application/json");
	        exchange.sendResponseHeaders(statusCode, responseBytes.length);

	        try (OutputStream os = exchange.getResponseBody();
	                PrintWriter out = new PrintWriter(os)) {
	            out.print(responseJson.toString());
	            out.flush();
	        }
	    }
		
		
		public static  void read(HttpExchange exchange) throws IOException, SQLException {
			try {
			String sql = "SELECT * FROM students WHERE id = ? ";
			Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
			int id = Integer.parseInt(params.get("id"));
		    Connection con =getDatabaseConnection();
		    PreparedStatement stmt=con.prepareStatement(sql);
		    stmt.setInt(1, id);
		    ResultSet rs = stmt.executeQuery();
		    if (rs.next()) {
		        JsonObject responseJson = new JsonObject();
		        responseJson.addProperty("id", rs.getInt("id"));
		        responseJson.addProperty("name", rs.getString("name"));
		        responseJson.addProperty("age", rs.getInt("age"));
		        sendJsonResponse(exchange, 200, responseJson);
		    }else {
		    	handleError(exchange, 404, "Student not found with id: " + id);
		    	
		    }
			}catch (SQLException e) {
		    	handleError(exchange, 400,e.getMessage());
		    	
		    }
		    
			
			
		}
		
		
		public static  void update(HttpExchange exchange) throws IOException, SQLException{
			try {
			String sql="UPDATE students SET name = ?, age = ? WHERE id = ?";
			String requestBody = new String(exchange.getRequestBody().readAllBytes());
	        JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();
	        int id = jsonObject.get("id").getAsInt();
	        String name = jsonObject.get("name").getAsString();
	        int age = jsonObject.get("age").getAsInt();
	        Connection con = getDatabaseConnection();
	        PreparedStatement stmt = con.prepareStatement(sql);
	        stmt.setString(1, name);
            stmt.setInt(2, age);
            stmt.setInt(3, id);
            int rowsAffected = stmt.executeUpdate();
            if(rowsAffected>0) {
            	JsonObject responseJson = jsonObject;
            	 sendJsonResponse(exchange, 200, responseJson);
            	
            }else {
            	handleError(exchange, 404, "Student not found for update with id: " + id);
            
            }
			}catch (SQLException e) {
		    	handleError(exchange, 400,e.getMessage());
		    	
		    }
            
           

			
		}
		
		
		public static void delete(HttpExchange exchange) throws IOException, SQLException{
			try {
			String sql="DELETE FROM students WHERE id = ?";
			String requestBody = new String(exchange.getRequestBody().readAllBytes());
	        JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();
	        int id = jsonObject.get("id").getAsInt();
	        Connection con = getDatabaseConnection();
	        PreparedStatement stmt = con.prepareStatement(sql);
	        stmt.setInt(1, id);
	        int rowsAffected = stmt.executeUpdate();
	        if(rowsAffected>0) {
	        	JsonObject responseJson = new JsonObject();
		        responseJson.addProperty("message", "Student deleted successfully");
		        sendJsonResponse(exchange, 200, responseJson);
	        }else {
	        	handleError(exchange, 404, "Student not found for update with id: " + id);
	        	
	        }
			}catch (SQLException e) {
		    	handleError(exchange, 400,e.getMessage());
		    	
		    }
	        
			
		}
		
		
		public static Map<String, String> queryToMap(String query) {
		    if (query == null) {
		        return null;
		    }
		    Map<String, String> result = new HashMap<>();
		    for (String param : query.split("&")) {
		        String[] entry = param.split("=");
		        if (entry.length > 1) {
		            try {
						result.put(
						    URLDecoder.decode(entry[0], "UTF-8"), 
						    URLDecoder.decode(entry[1], "UTF-8")
						);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        } else {
		            try {
						result.put(
						    URLDecoder.decode(entry[0], "UTF-8"),
						    ""
						);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    }
		    return result;
		}
		
		private static void handleError(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
	        JsonObject responseJson = new JsonObject();
	        responseJson.addProperty("error", errorMessage);
	        sendJsonResponse(exchange, statusCode, responseJson);
	    }

		
		
		
		
		public static void main(String[] args) throws IOException {
		 HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		 server.createContext("/api/greeting", (exchange -> {
			 
			 if ("POST".equals(exchange.getRequestMethod())) {
				 try {
					create(exchange);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			 }else if("GET".equals(exchange.getRequestMethod())) {
				 try {
						read(exchange);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 
			 }else if("PUT".equals(exchange.getRequestMethod())) {
				 try {
						update(exchange);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 
			 }else if("DELETE".equals(exchange.getRequestMethod())) {
				 try {
						delete(exchange);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 
			 }
			 
	       		 
	       		 
	       		 
	             else {
	                exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
	            }
	            exchange.close();
	        }));
		 
		 server.setExecutor(null); 
	        server.start();
		

	}

}