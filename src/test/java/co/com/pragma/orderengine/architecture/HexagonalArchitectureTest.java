package co.com.pragma.orderengine.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


@AnalyzeClasses(packages = "co.com.pragma.orderengine")
public class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_spring =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_jpa =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");

    @ArchTest
    static final ArchRule usecase_impls_must_depend_on_input_ports =
            classes().that().resideInAPackage("..domain.usecases..")
                    .and().haveSimpleNameEndingWith("UseCaseImpl")
                    .should().dependOnClassesThat().resideInAPackage("..domain.ports.in..");

    @ArchTest
    static final ArchRule driven_adapters_must_reside_in_their_package =
            classes().that().haveSimpleNameEndingWith("Adapter")
                    .should().resideInAPackage("..infrastructure.driven_adapters..");

    @ArchTest
    static final ArchRule controllers_must_reside_in_entry_points =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..infrastructure.entry_points..");
}
