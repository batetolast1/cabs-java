package io.legacyfighter.cabs;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.legacyfighter.cabs.entity.Aggregate;
import org.springframework.context.annotation.Configuration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.GeneralCodingRules.BE_ANNOTATED_WITH_AN_INJECTION_ANNOTATION;

@AnalyzeClasses(packages = "io.legacyfighter.cabs", importOptions = ImportOption.DoNotIncludeTests.class)
@SuppressWarnings("unused")
class CodingRulesTest {

    @ArchTest
    private final ArchRule NO_SOURCE_CLASSES_SHOULD_USE_FIELD_INJECTION = noFields()
            .that()
            .areDeclaredInClassesThat()
            .areNotAnnotatedWith(Configuration.class)
            .should(BE_ANNOTATED_WITH_AN_INJECTION_ANNOTATION)
            .as("no classes should use field injection")
            .because("field injection is considered harmful; use constructor injection or setter injection instead; "
                    + "see https://stackoverflow.com/q/39890849 for detailed explanations");

    @ArchTest
    private final ArchRule AGGREGATE_FIELDS_SHOULD_BE_ENCAPSULATED = fields()
            .that()
            .areDeclaredInClassesThat()
            .implement(Aggregate.class)
            .should()
            .bePrivate()
            .as("aggregate fields should be encapsulated")
            .because("aggregates should have only private fields to prevent direct modifications");

    @ArchTest
    private final ArchRule SERVICES_SHOULD_NOT_ACCESS_CONTROLLERS = noClasses()
            .that()
            .resideInAnyPackage("..service..")
            .should()
            .accessClassesThat()
            .resideInAPackage("..controller..")
            .as("services should not access controllers")
            .because("there should be no unnecessary dependencies in services");
}
