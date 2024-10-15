package github.elroy93;

import net.bytebuddy.jar.asm.Label;

/**
 * @author elroysu
 * @date 2024/10/14 20:55
 */public class LocalVariableNode {
    public String name;
    public int index;
    public String desc;
    public Label start;
    public Label end;


    public LocalVariableNode(String name, String descriptor, String signature, Label start, Label end, int index) {
        this.name = name;
        this.index = index;
        this.desc = descriptor;
        this.start = start;
        this.end = end;
    }
}
