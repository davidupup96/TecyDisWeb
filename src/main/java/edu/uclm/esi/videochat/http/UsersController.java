package edu.uclm.esi.videochat.http;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.videochat.model.Email;
import edu.uclm.esi.videochat.model.Manager;
import edu.uclm.esi.videochat.model.Message;
import edu.uclm.esi.videochat.model.Token;
import edu.uclm.esi.videochat.model.User;
import edu.uclm.esi.videochat.springdao.MessageRepository;
import edu.uclm.esi.videochat.springdao.TokenRepository;
import edu.uclm.esi.videochat.springdao.UserRepository;


@RestController
@RequestMapping("users")
public class UsersController {
	
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private TokenRepository tokenRepo;
	@Autowired
	private MessageRepository messageRepo;
	
	@PostMapping(value = "/login")
	public User login(HttpServletRequest request, @RequestBody Map<String, Object> credenciales) throws Exception {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString("name");
		String pwd = jso.getString("pwd");
		String ip = request.getRemoteAddr();
		User user = userRepo.findByNameAndPwd(name, pwd);
		if (user==null)
			throw new Exception("Incorrect login");
		Manager.get().add(user);
		request.getSession().setAttribute("user", user);
		Manager.get().add(request.getSession());
		return user;
	}
	
	@PutMapping("/register")
	public void register(@RequestBody Map<String, Object> credenciales) throws Exception {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString("name");
		String email = jso.getString("email");
		String pwd1 = jso.getString("pwd1");
		String pwd2 = jso.getString("pwd2");
		if (!pwd1.equals(pwd2))
			throw new Exception("Error: las contraseñas no coinciden");
		User user = new User();
		user.setEmail(email);
		user.setName(name);
		user.setPwd(pwd1);
		user.setConfirmationDate(0l);
		String picture = jso.optString("picture");
		user.setPicture(picture);
		userRepo.save(user);
		
		Token token = new Token(email);
		tokenRepo.save(token);
		Email sender=new Email();
		sender.send(email, "Bienvenido al Videochat", 
			"Para confirmar, pulse aquí: " +
			"https://localhost:7500/users/confirmarCuenta?tokenId=" + token.getId() +
			" o aquí: " +
			"https://localhost:7500/users/confirmarCuenta2/" + token.getId());
	}
	
	@GetMapping("/confirmarCuenta2/{tokenId}")
	public void confirmarCuenta2(HttpServletRequest request, HttpServletResponse response, @PathVariable String tokenId) throws IOException {
		// Ir a la base de datos, buscar el token con ese tokenId en la tabla, ver que no ha caducado
		// y actualizar la confirmationDate del user
		System.out.println(tokenId);
		response.sendRedirect("https://localhost:7500/");
	}
	
	@GetMapping("/confirmarCuenta")
	public void confirmarCuenta(HttpServletRequest request, HttpServletResponse response, @RequestParam String tokenId) throws IOException {
		// Ir a la base de datos, buscar el token con ese tokenId en la tabla, ver que no ha caducado
		// y actualizar la confirmationDate del user
		System.out.println(tokenId);
		response.sendRedirect("https://localhost:7500/");
	}
	
	@PatchMapping("/cambiarPwd")
	public void cambiarPwd(@RequestBody Map<String, String> credenciales) throws Exception {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString("name");
		String pwd = jso.getString("pwd");
		String pwd1 = jso.getString("pwd1");
		String pwd2 = jso.getString("pwd2");
		if (userRepo.checkPassword(name, pwd) > 0) { 
			if (pwd1.equals(pwd2)) {
				User user = userRepo.findByNameAndPwd(name, pwd);
				user.setPwd(pwd2);
				userRepo.save(user);

			} else throw new Exception("Las paasswords no coinciden");
		} else 
			throw new Exception("Credenciales inválidas");
	}
	
	@GetMapping(value = "/getUsuariosConectados", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getUsuariosConectados() {
		return Manager.get().getUsuariosConectados();
	}
	
	@PostMapping(value = "/Conversacion", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Message> getConversacion(HttpServletRequest request, @RequestBody Map<String, Object> credenciales) {
		JSONObject jso = new JSONObject(credenciales);
		String recipient = jso.getString("recipiente");
		String sender = jso.getString("destinatario");
		return Manager.get().getConversacion(recipient,sender);
	}
	@DeleteMapping(value = "/deleteHistorial", produces = MediaType.APPLICATION_JSON_VALUE)
	public void delHistorial(@RequestBody Map<String, Object> credenciales) {
		JSONObject jso = new JSONObject(credenciales);
		String recipient = jso.getString("recipiente");
		String sender = jso.getString("destinatario");
		Vector<Message> mensajes = messageRepo.findByRecipiente(recipient, sender);
		messageRepo.deleteAll(mensajes);
	}
	
	@GetMapping(value = "/getUsuarios", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<User> getUsuarios() {
		return Manager.get().getUsuarios();
	}
	
	@PostMapping(value="/getPicture", produces = MediaType.APPLICATION_JSON_VALUE)
	public String  getPicture(@RequestBody Map<String, Object> datos) {
		JSONObject jso = new JSONObject(datos);
		
		String name = jso.getString("name");
		
		
		JSONObject jsoResponse = new JSONObject();
		jsoResponse.put("picture", Manager.get().getPicture(name));
		return jsoResponse.toString();
		
	}
	
}

