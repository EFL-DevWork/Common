package com.thoughtworks.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;

public class YamlPropertySourceFactoryTest {
    private YamlPropertySourceFactory factory;

    @Before
    public void setUp() {
        factory = new YamlPropertySourceFactory();
    }

    @Test
    public void shouldCreateAPropertySourceFromTheNameProvided() throws IOException {
        String expectedPropertySourceName = "bootstrap.yml";
        EncodedResource res = new EncodedResource(new ClassPathResource("bootstrap-test.yml"), "UTF-8");

        PropertySource propertiesPropertySource = factory.createPropertySource(expectedPropertySourceName, res);

        assertNotNull(propertiesPropertySource);
        assertEquals(expectedPropertySourceName, propertiesPropertySource.getName());
    }

    @Test
    public void shouldCreatePropertySourceWithResourceFileNameWhenDefaultNameNotProvided() throws IOException {
        EncodedResource res = new EncodedResource(new ClassPathResource("bootstrap-test.yml"), "UTF-8");

        PropertySource propertiesPropertySource = factory.createPropertySource(null, res);

        assertNotNull(propertiesPropertySource);
        assertEquals("bootstrap-test.yml", propertiesPropertySource.getName());
    }

    @Test
    public void shouldCreatePropertySourceWithDefaultFileName() throws IOException {
        EncodedResource res = new EncodedResource(new ClassPathResource(""), "UTF-8");

        PropertySource propertiesPropertySource = factory.createPropertySource(null, res);

        assertNotNull(propertiesPropertySource);
        assertTrue(propertiesPropertySource.getName().contains("file-"));
    }

    @Test
    public void shouldThrowExceptionWhenPropertySourceNotFound() {
        EncodedResource res = new EncodedResource(new ClassPathResource("some-invalid-file"), "UTF-8");

        assertThrows(IOException.class, () -> factory.createPropertySource(null, res));
    }
}