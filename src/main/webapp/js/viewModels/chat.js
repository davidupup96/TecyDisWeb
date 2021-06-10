define(['knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils'],
		function(ko, app, moduleUtils, accUtils) {

	function ChatViewModel() {
		var self = this;
		
		this.user = app.user;
		
		self.recipient = ko.observable();

		self.chat = ko.observable(new Chat(ko));
		
		self.videoChat = ko.observable(new VideoChat(ko));

		self.estadoChatDeTexto = self.chat().estado;
		self.estadoSignaling = self.videoChat().estado;
		self.errorChatDeTexto = self.chat().error;
		self.errorSignaling = self.videoChat().error;

		// Header Config
		self.headerConfig = ko.observable({'view':[], 'viewModel':null});
		moduleUtils.createView({'viewPath':'views/header.html'}).then(function(view) {
			self.headerConfig({'view':view, 'viewModel': app.getHeaderModel()})
		})

		self.connected = function() {
			accUtils.announce('Chat page loaded.');
			document.title = "Chat";

			getUsuariosConectados();			
		};

		function getUsuariosConectados() {
			var data = {	
				url : "users/getUsuariosConectados",
				type : "get",
				contentType : 'application/json',
				success : function(response) {
					for (var i=0; i<response.length; i++) {
						var userName = response[i].name;
						var picture = response[i].picture;
						self.chat().addUsuario(userName, picture);
					}
				},
				error : function(response) {
					self.error(response.responseJSON.error);
				}
			};
			$.ajax(data);
		}
		
		
		self.getConversacion= function (destinatario) {
			
			var info = {
					recipiente : this.user._latestValue.name,
					destinatario : destinatario.nombre
				}
			
			var data = {	
				data : JSON.stringify(info),
				url : "users/Conversacion", 
				type : "post",
				contentType : 'application/json',
				success : function(response) {
					for (var i=0; i<response.length; i++) {
						if(i==0){
							self.chat().addMensaje("Mensajes antiguos", "Hora");
						}
						var hora=response[i].date;
						var mensaje = response[i].message;
						self.chat().addMensaje(mensaje, hora);
						if(i==response.length-1){
							self.chat().addMensaje("Fin de mensajes antiguos", "Hora");
						}
					}
				},
				
			};
			$.ajax(data);
		}
		
		self.deleteHistorial= function (destinatario) {
			
			var info = {
					recipiente : this.user._latestValue.name,
					destinatario : destinatario.nombre
				}
			
			var data = {	
				data : JSON.stringify(info),
				url : "users/deleteHistorial", 
				type : "delete",
				contentType : 'application/json',
				success : function() {
				window.alert("El historial fue borrado de la BD")
				}
			};
			$.ajax(data);
		}
	
		
		
		self.encenderVideoLocal = function() {
			self.videoChat().encenderVideoLocal();
		}
		
		self.crearConexion = function() {
			self.videoChat().crearConexion();
		}

		self.enviarOferta = function(destinatario) {
			self.videoChat().enviarOferta(destinatario.nombre);
		}
		
		self.disconnected = function() {
			self.chat().close();
		};

		self.transitionCompleted = function() {
			// Implement if needed
		};
	}

	return ChatViewModel;
}
);
