package pe.edu.cibertec.patitas_frontend_wc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc.client.LogoutClient;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.viewmodel.LoginModel;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginControllerAsync {

    @Autowired
    WebClient webClientAutenticacion;

    @PostMapping("/autenticar-async")
    public Mono<LoginResponseDTO> autenticar(@RequestBody LoginRequestDTO loginRequestDTO) {
        //validar campos de entrada
        if(loginRequestDTO.tipoDocumento() == null || loginRequestDTO.tipoDocumento().trim().length()==0
                ||loginRequestDTO.numeroDocumento() == null || loginRequestDTO.numeroDocumento().trim().length()==0
                || loginRequestDTO.password() == null || loginRequestDTO.password().trim().length()==0){
            LoginModel loginModel = new LoginModel("01","Error: Debe completar correctamente sus credenciales","");

            return Mono.just(new LoginResponseDTO("01","Error: Debe completar correctamente sus credenciales","","",
                    "",""));
        }
        try {
            //Invocar Api
            return webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginResponseDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class)
                    .flatMap(response ->{
                        if(response.codigo().equals("00")){

                            return Mono.just(new LoginResponseDTO("00","Login Exitoso",response.tipoDocumento(), response.numeroDocumento(),response.nombreUsuario(),response.correoUsuario()));
                        }else {

                            return Mono.just(new LoginResponseDTO("02","Error, Autenticacion fallida","","","",""));
                        }
                    });
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Mono.just(new LoginResponseDTO("99","Error: Error en la autenticacion","","","",""));
        }
    }


    @PostMapping("/logout-async")
    public Mono<LogoutResponseDTO> logout(@RequestBody LogoutRequestDTO logoutRequestDTO) {

        if(logoutRequestDTO.tipoDocumento() == null || logoutRequestDTO.tipoDocumento().trim().length()==0
                ||logoutRequestDTO.numeroDocumento() == null || logoutRequestDTO.numeroDocumento().trim().length()==0) {

            return Mono.just(new LogoutResponseDTO(false,null,"Error: Debe completar correctamente sus credenciales"
            ));
        }
        try {
            //Invocar Api
            return webClientAutenticacion.post()
                    .uri("/logout")
                    .body(Mono.just(logoutRequestDTO), LogoutResponseDTO.class)
                    .retrieve()
                    .bodyToMono(LogoutResponseDTO.class)
                    .flatMap(response ->{
                        if(response.resultado().equals(true)){
                            System.out.println("Sesión cerrada con WebFlux: ");
                            System.out.println("Resultado: " + response.resultado());
                            System.out.println("Fecha de cierre: " + response.fecha());

                            return Mono.just(new LogoutResponseDTO(true,response.fecha(),response.mensajeError()));

                        }else {

                            return Mono.just(new LogoutResponseDTO(false,null,"Error: No se pudo cerrar sesion"));
                        }
                    });
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Mono.just(new LogoutResponseDTO(false,null,"Error: Error en el logout"));
        }
    }
}
