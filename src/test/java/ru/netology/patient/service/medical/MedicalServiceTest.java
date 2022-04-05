package ru.netology.patient.service.medical;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertServiceImpl;

import java.math.BigDecimal;
import java.util.stream.Stream;


public class MedicalServiceTest {
    PatientInfoFileRepository patientInfoFileRepository;
    SendAlertServiceImpl sendAlertService;
    PatientInfo patientInfo;
    HealthInfo healthInfo;

    @BeforeEach
    public void init() {
        System.out.println("test started");
        healthInfo = Mockito.mock(HealthInfo.class);
        patientInfo = Mockito.mock(PatientInfo.class);
        patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        sendAlertService = Mockito.spy(SendAlertServiceImpl.class);

    }

    @BeforeAll
    public static void started() {
        System.out.println("started MedicalServiceTest");
    }

    @AfterEach
    public void finished() {
        System.out.println("test completed");
    }

    @AfterAll
    public static void finishedAll() {
        System.out.println("tests completed");
    }

    @ParameterizedTest
    @MethodSource("sourceCheckBloodPressure")
    public void testCheckBloodPressure(String patientId, BloodPressure bloodPressure) {
        //создаём значения давления для пациента
        BloodPressure bloodPressure1 = new BloodPressure();

        //метод строится на получении данных patientInfo.getHealthInfo().getBloodPressure()
        //потому создаём сначала заглушку на healthInfo(метод getBloodPressure()), потом с помощью это заглушки
        //создаём заглушку на patientInfo(метод getHealthInfo())

        Mockito.when(healthInfo.getBloodPressure()).thenReturn(bloodPressure1);
        Mockito.when(patientInfo.getHealthInfo()).thenReturn(healthInfo);
        //конечная заглушка на patientInfoFileRepository(метод getPatientInfo)
        Mockito.when(patientInfoFileRepository.getById(Mockito.any())).thenReturn(patientInfo);
        MedicalServiceImpl medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkBloodPressure(patientId, bloodPressure);
        //проверяем вызов метода send (вызывается в случае равенства давления пациента с проверяемым)
        Mockito.verify(sendAlertService, Mockito.only()).send(Mockito.any());
    }


    public static Stream<Arguments> sourceCheckBloodPressure() {
        return Stream.of(Arguments.of("", new BloodPressure(110, 90)),
                Arguments.of("", new BloodPressure(120, 100)));
    }


    @ParameterizedTest
    @MethodSource("sourceCheckTemperature")
    public void testCheckTemperature(String patientId, BigDecimal temperature) {
        BigDecimal comparedTemperature = new BigDecimal(40);
        Mockito.when(healthInfo.getNormalTemperature()).thenReturn(comparedTemperature);
        Mockito.when(patientInfo.getHealthInfo()).thenReturn(healthInfo);
        Mockito.when(patientInfoFileRepository.getById(patientId)).thenReturn(patientInfo);
        MedicalServiceImpl medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkTemperature(patientId, temperature);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sendAlertService, Mockito.only()).send(argumentCaptor.capture());
    }

    public static Stream<Arguments> sourceCheckTemperature() {
        return Stream.of(Arguments.of("", new BigDecimal(33)),
                Arguments.of("", new BigDecimal(36)),
                Arguments.of("", new BigDecimal(41)));
    }

    @ParameterizedTest
    @MethodSource("sourceCheckNoMessage")
    public void testCheckNoMessage(String patientId, BloodPressure bloodPressure, BigDecimal temperature) {
        Mockito.when(healthInfo.getNormalTemperature()).thenReturn(temperature);
        Mockito.when(healthInfo.getBloodPressure()).thenReturn(bloodPressure);
        Mockito.when(patientInfo.getHealthInfo()).thenReturn(healthInfo);
        Mockito.when(patientInfoFileRepository.getById(patientId)).thenReturn(patientInfo);
        MedicalServiceImpl medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkTemperature(patientId, temperature);
        medicalService.checkBloodPressure(patientId, bloodPressure);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sendAlertService, Mockito.never()).send(argumentCaptor.capture());
    }

    public static Stream<Arguments> sourceCheckNoMessage() {
        return Stream.of(Arguments.of("", new BloodPressure(120, 100), new BigDecimal(33)),
                Arguments.of("", new BloodPressure(120, 100), new BigDecimal(36)),
                Arguments.of("", new BloodPressure(120, 100), new BigDecimal(41)));
    }
}
