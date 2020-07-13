package ar.com.ada.api.billeteravirtual;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ar.com.ada.api.billeteravirtual.entities.Usuario;
import ar.com.ada.api.billeteravirtual.entities.Transaccion.ResultadoTransaccionEnum;
import ar.com.ada.api.billeteravirtual.security.Crypto;
import ar.com.ada.api.billeteravirtual.services.*;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	UsuarioService usuarioService;

	@Autowired
	BilleteraService billeteraService;

	@Test
	void EncryptionTest() {

		String textoClaro = "Este es un texto que todos pueden leer";

		// aca va algo que sepamos que cambie por cada usuario o transaccion
		String unSaltoLoco = "un numero random";

		// Aca vamos a dejar el texto encriptado(reversible!)
		String textoEncriptado = "";

		textoEncriptado = Crypto.encrypt(textoClaro, unSaltoLoco);

		// Este print no lo hagan en los testing reales! si bien sirve para buscar, lo
		// mejor es
		// tenerlos desactivados! En tal caso debuguean!
		System.out.println("el texto encriptado es: " + textoEncriptado);

		// Aca vamos a dejar el texto desencriptado de "textoEncryptado"
		String textoDesencriptado = "";

		// Desencripto!!
		textoDesencriptado = Crypto.decrypt(textoEncriptado, unSaltoLoco);

		// Todo va a estar bien, si el "textoClaro" es igual al "textoDesencriptado";
		assertTrue(textoClaro.equals(textoDesencriptado));

	}

	@Test
	void HashTest() {

		String textoClaro = "Este es un texto que todos pueden leer";

		// aca va algo que sepamos que cambie por cada usuario o transaccion
		String unSaltoLoco = "algo atado al usuario, ej UserId 20";

		// Aca vamos a dejar el texto hasheado(NO reversible)
		String textoHasheado = "";

		textoHasheado = Crypto.hash(textoClaro, unSaltoLoco);

		// Este print no lo hagan en los testing reales! si bien sirve para buscar, lo
		// mejor es
		// tenerlos desactivados! En tal caso debuguean!
		System.out.println("el texto hasheado es: " + textoHasheado);

		// Aca vamos a dejar el texto desencriptado de "textoEncryptado"
		String hashEsperado = "lxT/9Zj6PUyV/xTfCS90qfLMNEL7wnvg8VxsG/slFvZghZvQvFCZQvg584s6TMlkHqJ3wMA2J9rofsERmKGSUg==";

		// Todo va a estar bien, si el hash del texto es el
		assertTrue(textoHasheado.equals(hashEsperado));

	}

	@Test
	void CrearUsuarioTest() {

		Usuario usuario = usuarioService.crearUsuario("Karen", 32, 5, "21231123", new Date(), "karen@gmail.com",
				"a12345");

		// System.out.println("SALDO de usuario: " +
		// usuario.getPersona().getBilletera().getCuenta("ARS").getSaldo());

		// Usuario usuarioVerificado =
		// usuarioService.buscarPorUsername(usuario.getUsername());

		// assertTrue(usuario.getUsuarioId() == usuarioVerificado.getUsuarioId());
		assertTrue(usuario.getUsuarioId() > 0);
		assertTrue(usuario.getPersona().getBilletera().getCuenta("ARS").getSaldo().equals(new BigDecimal(500)));

	}

	@Test
	void EnviarSaldoMonedaARSTest() {

		Usuario usuarioEmisor = usuarioService.crearUsuario("Karen Envia", 32, 5, "21231123", new Date(),
				"karenenvia@gmail.com", "a12345");
		Usuario usuarioReceptor = usuarioService.crearUsuario("Claudia Recibe", 32, 5, "21231123", new Date(),
				"claudiarecibe@gmail.com", "a12345");

		Integer borigen = usuarioEmisor.getPersona().getBilletera().getBilleteraId();
		Integer bdestino = usuarioReceptor.getPersona().getBilletera().getBilleteraId();

		BigDecimal saldoOrigen = usuarioEmisor.getPersona().getBilletera().getCuenta("ARS").getSaldo();
		BigDecimal saldoDestino = usuarioReceptor.getPersona().getBilletera().getCuenta("ARS").getSaldo();

		BigDecimal saldoAEnviar = new BigDecimal(200);

		ResultadoTransaccionEnum resultado = billeteraService.enviarSaldo(saldoAEnviar, "ARS", borigen, bdestino,
				"PRESTAMO", "ya no me debes nada");

		BigDecimal saldoOrigenActualizado = billeteraService.consultarSaldo(borigen, "ARS");
		BigDecimal saldoDestinoActualizado = billeteraService.consultarSaldo(bdestino, "ARS");

		// AFIRMAMOS QUE, el saldo origen - 1200, sea igual al saldoOrigeActualizado
		// AFIRMAMOS QUE, el saldo destino + 1200, sea igual al saldoDestinoActualizado
		// System.out.println("SOrigen: " + saldoOrigen + " actualizado: " +
		// saldoOrigenActualizado);
		// System.out.println("SDestino: " + saldoDestino + " actualizado: " +
		// saldoDestinoActualizado);

		// 2 equals 2.0 => false
		// 2.0 equals 2.0 => true
		// 2.0 equals 2.00 => false
		// 2.00
		// se usa el compare, que devuelve 0 si son iguales, -1 si el primero es menor
		// que el segundo
		// y 1 si el primero es mayor que segundo.
		assertTrue(resultado == ResultadoTransaccionEnum.INICIADA, "El resultado fue " + resultado);

		assertTrue(saldoOrigen.subtract(saldoAEnviar).compareTo(saldoOrigenActualizado) == 0,
				" HUBO error en la comparacion SOrigen: " + saldoOrigen + " actualizado: " + saldoOrigenActualizado);
		assertTrue(saldoDestino.add(saldoAEnviar).compareTo(saldoDestinoActualizado) == 0,
				" HUBO error en la comparacion SDestino: " + saldoDestino + " actualizado: " + saldoDestinoActualizado);

	}

	@Test
	void EnviarSaldoMonedaUSDSinSALDOUSDTest() {

		Usuario usuarioEmisor = usuarioService.crearUsuario("Karen Envia", 32, 5, "21231123", new Date(),
				"karenenvia@gmail.com", "a12345");
		Usuario usuarioReceptor = usuarioService.crearUsuario("Claudia Recibe", 32, 5, "21231123", new Date(),
				"claudiarecibe@gmail.com", "a12345");

		Integer borigen = usuarioEmisor.getPersona().getBilletera().getBilleteraId();
		Integer bdestino = usuarioReceptor.getPersona().getBilletera().getBilleteraId();

		BigDecimal saldoAEnviar = new BigDecimal(200);

		ResultadoTransaccionEnum resultado = billeteraService.enviarSaldo(saldoAEnviar, "USD", borigen, bdestino,
				"PRESTAMO", "ya no me debes nada");

		assertTrue(resultado == ResultadoTransaccionEnum.SALDO_INSUFICIENTE, "El resultado fue " + resultado);

	}

	@Test
	void EnviarSaldoNegativoTest() {

		Usuario usuarioEmisor = usuarioService.crearUsuario("Karen Envia", 32, 5, "21231123", new Date(),
				"karenenvia@gmail.com", "a12345");
		Usuario usuarioReceptor = usuarioService.crearUsuario("Claudia Recibe", 32, 5, "21231123", new Date(),
				"claudiarecibe@gmail.com", "a12345");

		Integer borigen = usuarioEmisor.getPersona().getBilletera().getBilleteraId();
		Integer bdestino = usuarioReceptor.getPersona().getBilletera().getBilleteraId();

		ResultadoTransaccionEnum resultado = billeteraService.enviarSaldo(new BigDecimal(-1200), "ARS", borigen,
				bdestino, "PRESTAMO", "ya no me debes nada");

		assertTrue(resultado == ResultadoTransaccionEnum.ERROR_IMPORTE_NEGATIVO, "El resultado fue " + resultado);

	}

}
