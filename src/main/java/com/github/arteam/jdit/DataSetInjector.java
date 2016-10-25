package com.github.arteam.jdit;

import com.github.arteam.jdit.annotations.DataSet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Date: 2/1/15
 * Time: 3:24 PM
 * <p>
 * Component which is responsible for injecting test data to methods.
 * It analyzes test methods and classes for the {@link DataSet} annotation
 * and dispatches locations of the data to the {@link DataMigration}
 * component.
 *
 * @author Artem Prigoda
 */
class DataSetInjector {

    private DataMigration dataMigration;

    DataSetInjector(DataMigration dataMigration) {
        this.dataMigration = dataMigration;
    }

    /**
     * Injects test data to a method.
     * <p>
     * If the method or class has the {@link DataSet} annotation, data from the scripts of the locations,
     * specified in the annotation, will be injected to the DB
     *
     * @param method the current method
     */
    void injectData(Method method) {
        DataSet classLevelDataSet = method.getDeclaringClass().getAnnotation(DataSet.class);
        DataSet methodDataSet = method.getAnnotation(DataSet.class);
        DataSet actualDataSet = methodDataSet != null ? methodDataSet : classLevelDataSet;
        if (actualDataSet != null) {
            if (actualDataSet.value().length > 0) {
                String[] scriptLocations = actualDataSet.value();
                for (String location : scriptLocations) {
                    dataMigration.executeScript(location);
                }
            } else if (!actualDataSet.directory().isEmpty()) {
                try {
                    Files
                            .list(Paths.get(actualDataSet.directory()))
                            .filter(path -> path.endsWith(".sql"))
                            .map(String::valueOf)
                            .sorted()
                            .forEach(x -> dataMigration.executeScript(x.replace("src/main/resources/", "")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
