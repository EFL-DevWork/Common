package com.thoughtworks.user.model;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.user.api.UserDetailsResp;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaskingSerializerTest {


    @Test
    public void jsonMasking() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UserDetailsResp u = mapper.readValue("{\n" +
                "\t\"userId\":\"narendra4\",\n" +
                "\t\"firstName\":\"naren\",\n" +
                "\t\"taxId\":\"tax_id3\",\n" +
                "\t\"lastName\":\"kumar\",\n" +
                "\t\"dob\":\"26/05/1991\"\n" +
                "}", UserDetailsResp.class);


        Assertions.assertThat(mapper.writeValueAsString(u)).isEqualTo("{\"userId\":\"narendra4\",\"firstName\":\"naren\",\"lastName\":\"kumar\",\"dob\":\"26/05/1991\",\"taxId\":\"***\"}");
    }


    // start of test data for nestedObjectDataMasking

    public static class NestedClass2 {
        @Mask
        public String data;

        public NestedClass2() {

        }
    }

    public static class NestedClass1 {
        public String data;
        public NestedClass2 class2 = new NestedClass2();
        public List<NestedClass2> list = new ArrayList<>();

        public NestedClass1() {

        }
    }

    public static class Root {
        @Mask
        public String data;
        public NestedClass1 nestedClass1 = new NestedClass1();

        public Root() {

        }
    }

    // end of test data for nestedObjectDataMasking


    @Test
    public void nestedObjectDataMasking() throws JsonProcessingException {


        Root root = new Root();
        root.data = "root data";
        root.nestedClass1.data = "nested class 1 data";
        root.nestedClass1.class2.data = " nested class 2 data";
        NestedClass2 item1 = new NestedClass2();
        NestedClass2 item2 = new NestedClass2();
        root.nestedClass1.list.add(item1);
        item1.data = "item 1 data";
        item2.data = "item 2 data";
        root.nestedClass1.list.add(item2);

        ObjectMapper mapper = new ObjectMapper();
        Root maskedRoot = mapper.readValue(mapper.writeValueAsString(root), Root.class);
        Assertions.assertThat(maskedRoot.data).isEqualTo(Mask.DEFAULT_MASK);
        Assertions.assertThat(maskedRoot.nestedClass1.data).isEqualTo(root.nestedClass1.data);
        Assertions.assertThat(maskedRoot.nestedClass1.class2.data).isEqualTo(Mask.DEFAULT_MASK);
        Assertions.assertThat(maskedRoot.nestedClass1.list.get(0).data).isEqualTo(Mask.DEFAULT_MASK);
        Assertions.assertThat(maskedRoot.nestedClass1.list.get(1).data).isEqualTo(Mask.DEFAULT_MASK);
    }


    // start of test data for customMaskingTest

    public static class CreditCardMask implements DataMask {
        @Override
        public String mask(String unMaskedData) {
            if (unMaskedData.length() > 4) {
                return unMaskedData.substring(0, 4) + unMaskedData.substring(4).replaceAll("[0-9]", "*");
            }
            return unMaskedData.replaceAll("[0-9]", "*");
        }
    }


    public static class CardInfo {
        @Mask
        public String userName;
        @Mask(maskType = CreditCardMask.class)
        public String creditCardNumber;
        @Mask(defaultMaskValue = "$$$$$")
        public String cvv;
        public String userId;

    }


    // end of test data for customMaskingTest


    @Test
    public void customMaskingTest() throws JsonProcessingException {

        CardInfo cardInfo = new CardInfo();
        cardInfo.creditCardNumber = "1234 5678 9012 3456";
        cardInfo.userName = "test user";
        cardInfo.cvv = "123";
        cardInfo.userId = "user1";
        ObjectMapper mapper = new ObjectMapper();
        CardInfo maskedCardInfo = mapper.readValue(mapper.writeValueAsString(cardInfo), CardInfo.class);
        Assertions.assertThat(maskedCardInfo.userName).isEqualTo(Mask.DEFAULT_MASK);
        Assertions.assertThat(maskedCardInfo.creditCardNumber).isEqualTo("1234 **** **** ****");
        Assertions.assertThat(maskedCardInfo.cvv).isEqualTo("$$$$$");
        Assertions.assertThat(maskedCardInfo.userId).isEqualTo(cardInfo.userId);
    }


    // start test data for failsIfMaskAnnotationIsAppliedOnNonStringDataTypes

    public static class InvalidMaskTest {
        @Mask
        public int data;
    }

    // end test data for failsIfMaskAnnotationIsAppliedOnNonStringDataTypes


    @Test
    public void failsIfMaskAnnotationIsAppliedOnNonStringDataTypes() {
        InvalidMaskTest test = new InvalidMaskTest();
        ObjectMapper mapper = new ObjectMapper();
        Assertions.assertThatExceptionOfType(JsonMappingException.class).as("@Mask annotation is only applicable for String fields")
                .isThrownBy(() -> {
                    mapper.writeValueAsString(test);
                });

    }

    // start test data for unableToInitializeAMask

    public static class PatternMask implements DataMask {
        private String mask ;

        public PatternMask(String pattern){
            this.mask = pattern;
        }
        @Override
        public String mask(String unMaskedData) {
            return this.mask;
        }
    }

    public static class InvalidMaskTypeTest {
        @Mask(maskType = PatternMask.class)
        public String data;
    }

    @Test
    public void failsIfDesiredMaskTypeCouldnotBeInitialized() throws JsonProcessingException {
        InvalidMaskTypeTest test = new InvalidMaskTypeTest();
        ObjectMapper mapper = new ObjectMapper();
        test.data = "data";
        Logger logger = (Logger) LoggerFactory.getLogger(MaskingSerializer.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        List<ILoggingEvent> logsList = listAppender.list;

        mapper.writeValueAsString(test);

        assertEquals(test.data, "data");
        ILoggingEvent errorLog = logsList.get(0);
        assertEquals(Level.ERROR, errorLog.getLevel());
        assertTrue(errorLog.getMessage().contains("Exception while masking the data"));

    }
}