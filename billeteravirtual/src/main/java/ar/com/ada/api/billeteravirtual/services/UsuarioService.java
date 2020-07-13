package ar.com.ada.api.billeteravirtual.services;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import ar.com.ada.api.billeteravirtual.entities.Billetera;
import ar.com.ada.api.billeteravirtual.entities.Cuenta;
import ar.com.ada.api.billeteravirtual.entities.Persona;
import ar.com.ada.api.billeteravirtual.entities.Usuario;
import ar.com.ada.api.billeteravirtual.repositories.UsuarioRepository;
import ar.com.ada.api.billeteravirtual.security.Crypto;
@Service
public class UsuarioService {

    @Autowired
    PersonaService personaService;
    @Autowired
    BilleteraService billeteraService;
    @Autowired
    UsuarioRepository usuarioRepository;

    public Usuario buscarPorUsername(String username) {
      return usuarioRepository.findByUsername(username);
    }
  
    public void login(String username, String password) {
      
      /**Metodo IniciarSesion
      * recibe usuario y contraseña
      * validar usuario y contraseña
      */
  
      Usuario u = buscarPorUsername(username);
  
      if (u == null || !u.getPassword().equals(Crypto.encrypt(password, u.getUsername()))) {
  
        throw new BadCredentialsException("Usuario o contraseña invalida");
      }
    }
    
    public Usuario crearUsuario(String nombre, int pais, int tipoDocumento, String documento, Date fechaNacimiento, String email, String password) {

    /**Metodo para crearUsuario
     * 1 crear persona (se le settea un usuario)
     * 2 crear usuario
     * 3 crear billetera 
     * 4 crear cuenta por moneda(ARS y/o USD?)
     */

      Persona persona = new Persona();
        persona.setNombre(nombre);
        persona.setPaisId(pais);
        persona.setTipoDocumentoId(tipoDocumento);
        persona.setDocumento(documento);
        persona.setFechaNacimiento(fechaNacimiento);

        Usuario usuario = new Usuario();
        usuario.setUsername(email);
        usuario.setEmail(email);
        usuario.setPassword(Crypto.encrypt(password,email));

        persona.setUsuario(usuario);

        personaService.grabar(persona);

        Billetera billetera = new Billetera();

        Cuenta pesos = new Cuenta();

        pesos.setSaldo(new BigDecimal(0));
        pesos.setMoneda("ARS");

        Cuenta dolares = new Cuenta();

        dolares.setSaldo(new BigDecimal(0));
        dolares.setMoneda("USD");

        billetera.agregarCuenta(pesos);
        billetera.agregarCuenta(dolares);

        persona.setBilletera(billetera);

        billeteraService.grabar(billetera);

        billeteraService.cargarSaldo(new BigDecimal(500), "ARS", billetera, "regalo", "Bienvenida por creacion de usuario");

        return usuario;
    }

    public Usuario buscarPorEmail(String email){
      
      return usuarioRepository.findByEmail(email);
  }

    
}