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
        validarMontoNegativo(cuanto);
        validarCantidadDepositosMaxima();
        agregarMovimiento(LocalDate.now(), cuanto, true);
    }

    public void sacar(double cuanto) {
        validarMontoNegativo(cuanto);
        validarSaldoMenor(cuanto);
        validarLimiteDiarioExtraccion(cuanto);
        agregarMovimiento(LocalDate.now(), cuanto, false);
    }

    public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
        Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
        procesarMovimiento(movimiento);
        movimientos.add(movimiento);
    }

    public void procesarMovimiento(Movimiento movimiento) {
        this.setSaldo(movimiento.calcularValor(this.getSaldo()));
    }

    public void validarCantidadDepositosMaxima() {
        if (getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count() >= MAX_DEPOSITOS_DIARIOS) {
            throw new MaximaCantidadDepositosException("Ya excedio los " + MAX_DEPOSITOS_DIARIOS + " depositos diarios");
        }
    }


    public void validarMontoNegativo(double monto) throws MontoNegativoException {
        if (monto <= 0) {
            throw new MontoNegativoException(monto + ": el monto a ingresar debe ser un valor positivo");
        }
    }

    public void validarLimiteDiarioExtraccion(double monto) throws MaximoExtraccionDiarioException {
        double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
        double limite = LIMITE_EXTRACCION - montoExtraidoHoy;
        if (monto > limite) {
            throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + LIMITE_EXTRACCION
                    + " diarios, límite: " + limite);
        }
    }

    public void validarSaldoMenor(double monto) throws SaldoMenorException {
        if (getSaldo() - monto < 0) {
            throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
        }
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
