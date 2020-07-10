package com.zheng;

import com.zheng.annotations.Autowired;
import com.zheng.annotations.Component;
import com.zheng.annotations.Value;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Zheng {

    private HashSet<Class> components;
    private HashMap<Class, Object> implementation;

    private HashMap<String, String> configMap;

    private Zheng(String... paths) throws Exception {
        components = new HashSet<Class>();
        implementation = new HashMap<Class, Object>();
        configMap = new HashMap<>();
        init(paths);
    }

    private void init(String... paths) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        loadConfig();
        findComponents(paths);
        injectFields(paths);
    }

    private void loadConfig() throws IOException {
        String path = getClass().getResource("/").getPath() + "application.properties";
        String config = new String(Files.readAllBytes(Paths.get(path)));
        String[] lines = config.split("[\n]");
        for (String line : lines) {
            String[] temp = line.split("[=]");
            configMap.put(temp[0].trim(), temp[1].trim());
        }
    }

    private void findComponents(String... paths) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for (String path : paths) {
            Reflections reflections = new Reflections(path);
            components.addAll(reflections.getTypesAnnotatedWith(Component.class));
        }
        for (Class current : components) {
            Constructor<?> componentConstructor = current.getConstructor();
            implementation.put(current, componentConstructor.newInstance());
        }
    }

    private void injectFields(String... paths) throws IllegalAccessException {
        for (String path : paths) {
            Reflections reflections = new Reflections(path, new FieldAnnotationsScanner());
            Set<Field> fields = reflections.getFieldsAnnotatedWith(Autowired.class);

            for (Field field : fields) {
                Class baseService = field.getDeclaringClass();
                Class autowiredService = field.getType();
                field.setAccessible(true);
                field.set(implementation.get(baseService), implementation.get(autowiredService));
            }

            fields = reflections.getFieldsAnnotatedWith(Value.class);

            for (Field field : fields) {
                Class injectInService = field.getDeclaringClass();
                field.setAccessible(true);
                if (field.getAnnotatedType().getType().getTypeName().equals("int")) {
                    field.set(implementation.get(injectInService), Integer.parseInt(configMap.get(field.getName())));
                } else {
                    field.set(implementation.get(injectInService), configMap.get(field.getName()));
                }
            }
        }
    }

    public static Zheng build(String... paths) throws Exception {
        return new Zheng(paths);
    }

    public void start() {
        for (Class service : components) {
            if (Runnable.class.isAssignableFrom(service)) {
                ((Runnable) implementation.get(service)).run();
            }
        }
    }
}
