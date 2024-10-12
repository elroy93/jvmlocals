package github.elroy93.jvmlocals.tool;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * @author elroysu
 * @date 2024/10/12 星期六 22:47
 */
public class JunitTools {

    public static void printJvmArgs() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();

        System.out.println("JVM启动参数:");
        for (String arg : arguments) {
            System.out.println(arg);
        }
    }

}
