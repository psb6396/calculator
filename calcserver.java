import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class calcserver {

    public static String calc(String exp) {
        StringTokenizer st = new StringTokenizer(exp, " ");
        if (st.countTokens() != 3)
            return "error";
        String res = "";
        int op1 = Integer.parseInt(st.nextToken());
        String opcode = st.nextToken();
        int op2 = Integer.parseInt(st.nextToken());
        switch (opcode) {
            case "+":
                res = Integer.toString(op1 + op2);
                break;
            case "-":
                res = Integer.toString(op1 - op2);
                break;
            case "*":
                res = Integer.toString(op1 * op2);
                break;
            // case "/":
            //     res = Integer.toString(op1 / op2);
            //     break;
            default:
                res = "error";
        }
        return res;
    }

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(5);

        try (ServerSocket listener = new ServerSocket(9999)) {
            System.out.println("Waiting for connections...");

            while (true) {
                Socket clientSocket = listener.accept();
                System.out.println("Connected: " + clientSocket.getInetAddress());

                // Create a new instance of CalculatorHandler for each client
                Runnable calculatorHandler = new CalculatorHandler(clientSocket);
                threadPool.execute(calculatorHandler);
            }
        } catch (IOException e) {
            System.out.println("Error in server: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}

class CalculatorHandler implements Runnable {
    private final Socket clientSocket;

    public CalculatorHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            while (true) {
                String inputMessage = in.readLine();
                if (inputMessage.equalsIgnoreCase("bye")) {
                    System.out.println("Client disconnected: " + clientSocket.getInetAddress());
                    break;
                }

                System.out.println(inputMessage);
                String res = calc(inputMessage);
                out.write(res + "\n");
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
