package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LombokEqualsMethodAnalyzerIntegrationTest extends AbstractSpoonIntegrationTest {

    @Mock
    private AccessibilityAwareFieldFinder fieldFinderMock;

    private LombokEqualsMethodAnalyzer testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new LombokEqualsMethodAnalyzer();
    }

    

}
