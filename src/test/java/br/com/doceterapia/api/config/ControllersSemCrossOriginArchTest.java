package br.com.doceterapia.api.config;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.CrossOrigin;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.doceterapia.api", importOptions = ImportOption.DoNotIncludeTests.class)
class ControllersSemCrossOriginArchTest {

    // Origins come from the single CORS configuration, so no controller may open its own.
    @ArchTest
    static final ArchRule controllersNaoUsamCrossOrigin = noClasses()
            .that().resideInAPackage("..controller..")
            .should().beAnnotatedWith(CrossOrigin.class)
            .orShould().dependOnClassesThat().areAssignableTo(CrossOrigin.class);
}
