package dds.monedero.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

public class Cuenta {

  private double saldo = 0;
  static final double MAX_DEPOSITOS_DIARIOS = 3; //entiendo que esto sera una constante. Otra alternativa es meterlo dentro del constructor
  static final double LIMITE_EXTRACCION = 1000; //Tambien otra variante seria meterlo dentro del constructor, si cada cuenta tiene limites distintos.

  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {
//    Code Smell 4 - se puede extraer en otra funcion (de hecho, se reutiliza)
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
//    Code smell 4:  Validar la cantidad de depositos podria extraerse en otra funcion
    if (getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count() >= MAX_DEPOSITOS_DIARIOS) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + MAX_DEPOSITOS_DIARIOS + " depositos diarios");
    }

    agregarMovimiento(LocalDate.now(), cuanto, true);
  }

  public void sacar(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

//    Code smell 4
    if (getSaldo() - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
    //Code smell 4: Toda la funcionalidad de saber si se excedio el limite podria sacarse en otra funcion. Asi como esta sacar() tiene
//    varias responsabilidades
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = LIMITE_EXTRACCION - montoExtraidoHoy;
    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + LIMITE_EXTRACCION
          + " diarios, límite: " + limite);
    }
    agregarMovimiento(LocalDate.now(), cuanto, false);
  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    procesarMovimiento(movimiento);
    movimientos.add(movimiento);
  }

  public void procesarMovimiento(Movimiento movimiento){
    this.setSaldo(movimiento.calcularValor(this.getSaldo()));
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

}
