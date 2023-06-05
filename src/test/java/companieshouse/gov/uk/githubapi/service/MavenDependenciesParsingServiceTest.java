package companieshouse.gov.uk.githubapi.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import companieshouse.gov.uk.githubapi.service.mvn.ParseRule;

@ExtendWith(MockitoExtension.class)
public class MavenDependenciesParsingServiceTest {
    

    @Mock
    private ParseRule ruleOne;

    @Mock
    private ParseRule ruleTwo;

    @Mock
    private DocumentBuilderFactory documentBuilderFactoryMock;

    @Mock
    private DocumentBuilder documentBuilderMock;

    @Mock
    private Document documentMock;

    private MavenDependenciesParsingService mavenDependenciesParsingService;

    private static String demoPom;

    @BeforeAll
    static void setupClass() throws Exception {
        final File pomFile = ResourceUtils.getFile("classpath:demopom.xml");
        
        demoPom = new String(Files.readAllBytes(pomFile.toPath()));
    }

    @BeforeEach
    void setup() throws Exception {
        mavenDependenciesParsingService = new MavenDependenciesParsingService(List.of(ruleOne, ruleTwo), documentBuilderFactoryMock);

        when(documentBuilderFactoryMock.newDocumentBuilder()).thenReturn(documentBuilderMock);

        // When running the error case it fails with unnecessary stubbings exception therefore this stub
        // is not neccessary for that case but it is for all other tests
        lenient().when(documentBuilderMock.parse(any(InputSource.class))).thenReturn(documentMock);
    }

    @Test
    void testParseDependenciesParsesThePomBeforePassingToEachRule() {
        mavenDependenciesParsingService.parseDependencies(demoPom);

        verify(ruleOne, times(1)).run(documentMock);
        verify(ruleTwo, times(1)).run(documentMock);
    }

    @Test
    void testParseDependenciesReturnsAllDependenciesReturnedFromRules() {
        Map<String, String> resultFromOne = Map.of(
            "spring", "3.0.0",
            "assertj", "2.4.1"
        );
        when(ruleOne.run(any())).thenReturn(resultFromOne);

        Map<String, String> resultFromTwo = Map.of(
            "junit", "4.0.0"
        );
        when(ruleTwo.run(any())).thenReturn(resultFromTwo);

        final Map<String, String> result = mavenDependenciesParsingService.parseDependencies(demoPom);

        assertThat(result).containsAllEntriesOf(resultFromOne).containsAllEntriesOf(resultFromTwo).hasSize(3);
    }

    @Test
    void testParseDependenciesThrowsPoorlyFormattedExceptionWhenNotParsable() throws Exception {
        when(documentBuilderMock.parse(any(InputSource.class))).thenThrow(new SAXException());

        assertThatThrownBy(() -> mavenDependenciesParsingService.parseDependencies("<bad")).hasMessageContaining("Could not parse POM").hasCauseInstanceOf(SAXException.class);
    }
}
