
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

class Cadeira {

    boolean livre = true;
    Cliente cliente;
}

class BarbeiroDorminhoco implements Runnable {

    boolean dormindo = false;
    Cliente cliente;

    BarbeiroDorminhoco() {
    }

    @Override
    public void run() {
        while (true) {
            if (dormindo) {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            } else {
                synchronized (Main.cadeiras) {
                    for (int i = 0; i < Main.cadeiras.size(); i++) {
                        if (Main.cadeiras.get(i).livre == false) {
                            System.out.println("Barbeiro está atendendo o Cliente " + Main.cadeiras.get(i).cliente.id);
                            try {
                                sleep(100);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(BarbeiroDorminhoco.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            Main.cadeiras.get(i).livre = true;
                            Main.cadeiras.get(i).cliente.atendido = true;
                            System.out.println("Barbeiro atendeu o cliente "+ Main.cadeiras.get(i).cliente.id);
                            synchronized (Main.cadeiras.get(i).cliente) {
                                Main.cadeiras.get(i).cliente.notify();
                                Main.cadeiras.get(i).cliente = null;
                            }

                        }
                    }
                }
                synchronized (this) {
                    try {
                        System.out.println("Barbeiro não tinha clientes e Dormiu");
                        this.dormindo = true;
                        this.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BarbeiroDorminhoco.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        }
    }
}

class Cliente implements Runnable {

    boolean esperando = false;
    int id;
    Cadeira p;
    boolean atendido = false;
    BarbeiroDorminhoco barbeiro;

    Cliente(BarbeiroDorminhoco babeiro, int id) {
        this.barbeiro = babeiro;
        this.id = id;
    }

    public void run() {
        synchronized (Main.cadeiras) {
            for (int i = 0; i < Main.cadeiras.size(); i++) {
                if (p == null && Main.cadeiras.get(i).livre == true) {
                    Main.cadeiras.get(i).livre = false;
                    this.p = Main.cadeiras.get(i);
                    Main.cadeiras.get(i).cliente = this;
                    System.out.println("Cliente " + id + " pegou a cadeira" + i);
                    break;
                }
            }
        }
        if (p == null) {
            System.out.println("O cliente " + this.id + " não encontrou cadeiras vazias e foi embora.");
            atendido = true;
        }
        while (!atendido) {
            synchronized (barbeiro) {
                while (barbeiro.dormindo) {
                    barbeiro.dormindo = false;
                    barbeiro.cliente = this;
                    barbeiro.notifyAll();
                    System.out.println(Thread.currentThread().getName() + " Acordou o barbeiro");

                }
            }
            if (atendido) {
                System.out.println("Cliente " + id + " está pronto e saindo do barbeiro");
            }
        }
    }
}

public class Main {

    public static ArrayList<Cadeira> cadeiras = new ArrayList<>();

    public static void main(String[] args) {
        int nCadeiras = 3;
        int nClientes = 5;
        for (int i = 0; i < nCadeiras; i++) {
            Cadeira cadeira = new Cadeira();
            cadeiras.add(cadeira);
        }
        BarbeiroDorminhoco barbeiro = new BarbeiroDorminhoco();
        Thread cons = new Thread(barbeiro, "Barbeiro");

        for (int i = 0; i < nClientes; i++) {
            Thread prod = new Thread(new Cliente(barbeiro, i), "Cliente " + i);
            prod.start();
        }
        cons.start();

    }
}
