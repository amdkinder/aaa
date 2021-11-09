package uz.devops.bm;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("uz.devops.bm");

        noClasses()
            .that()
            .resideInAnyPackage("uz.devops.bm.service..")
            .or()
            .resideInAnyPackage("uz.devops.bm.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..uz.devops.bm.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}
