package fr.tp.inf112.projects.robotsim.remote;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.canvas.model.Canvas;

public class Clientsocket {

    static Object roundtrip(String host, int port, Object payload) throws Exception {
        try (var sock = new Socket(host, port)) {
            var out = new ObjectOutputStream(sock.getOutputStream());
            out.flush();
            var in  = new ObjectInputStream(sock.getInputStream());
            out.writeObject(payload);
            out.flush();
            return in.readObject();
        }
    }

    static Factory makeFactoryWithId(String id) throws Exception {
        Factory f = Factory.class.getDeclaredConstructor().newInstance();
        try {
            Factory.class.getMethod("setId", String.class).invoke(f, id);
        } catch (NoSuchMethodException ignore) {
            System.out.println("cannot set id on Factory, method not found");
        }
        return f;
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 5050;

        if (args.length < 1) {
            System.out.println("usage: java Clientsocket <command> [args...]");
            System.out.println("  list");
            System.out.println("  persist <id>");
            System.out.println("  read <id>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list": {
                Object resp = roundtrip(host, port, Proto.LIST); 
                if (resp instanceof String[]) {
                    System.out.println("list of data: " + Arrays.toString((String[]) resp));
                } else {
                    System.out.println("response type: " + resp);
                }
                break;
            }
            case "persist": {
                if (args.length < 2) { System.out.println("persist 需要 <id>"); return; }
                String id = args[1];
                Factory f = makeFactoryWithId(id);               
                Object resp = roundtrip(host, port, f);        
                System.out.println("PERSIST 响应: " + resp + "（期望 true）");
                System.out.println("到模块根目录的 data\\ 查看是否生成: " + id);
                break;
            }
            case "read": {
                if (args.length < 2) { System.out.println("read 需要 <id>"); return; }
                String id = args[1];
                Object resp = roundtrip(host, port, id); 
                if (resp instanceof Canvas) {
                    System.out.println("READ 成功，得到 Canvas: " + resp.getClass().getName());
                    try {
                        var getId = resp.getClass().getMethod("getId");
                        System.out.println("Canvas.id = " + getId.invoke(resp));
                    } catch (NoSuchMethodException ignore) {}
                } else if (resp instanceof IOException) {
                    System.out.println("READ 失败: " + ((IOException) resp).getMessage());
                } else {
                    System.out.println("意外响应类型: " + resp);
                }
                break;
            }
            default:
                System.out.println("未知命令: " + args[0]);
        }
    }
}
