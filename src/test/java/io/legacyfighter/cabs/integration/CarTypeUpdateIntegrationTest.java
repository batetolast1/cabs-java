package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.dto.CarTypeDTO;
import io.legacyfighter.cabs.entity.CarType;
import io.legacyfighter.cabs.service.CarTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.legacyfighter.cabs.entity.CarType.CarClass.VAN;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CarTypeUpdateIntegrationTest {

    @Autowired
    private CarTypeService carTypeService;

    @Test
    void canCreateCarType() {
        // given
        thereIsNoCarClassInTheSystem(VAN);

        // when
        CarTypeDTO created = createCarClass(VAN, "Description");

        // then
        CarTypeDTO loaded = carTypeService.loadDto(created.getId());
        assertThat(loaded.getCarClass()).isEqualTo(VAN);
        assertThat(loaded.getDescription()).isEqualTo("Description");
        assertThat(loaded.getStatus()).isEqualTo(CarType.Status.INACTIVE);
        assertThat(loaded.getCarsCounter()).isZero();
        assertThat(loaded.getMinNoOfCarsToActivateClass()).isEqualTo(10);
        assertThat(loaded.getActiveCarsCounter()).isZero();
    }

    @Test
    void canChangeCarDescription() {
        // given
        thereIsNoCarClassInTheSystem(VAN);
        // and
        createCarClass(VAN, "description");

        // when
        CarTypeDTO created = createCarClass(VAN, "New description");

        // then
        CarTypeDTO loaded = carTypeService.loadDto(created.getId());
        assertThat(loaded.getDescription()).isEqualTo("New description");
    }

    @Test
    void canRegisterActiveCars() {
        // given
        CarTypeDTO created = createCarClass(VAN, "description");
        // and
        int currentActiveCarsCount = load(created.getId()).getActiveCarsCounter();

        // when
        registerActiveCar(VAN);

        // then
        CarTypeDTO loaded = carTypeService.loadDto(created.getId());
        assertThat(loaded.getActiveCarsCounter()).isEqualTo(currentActiveCarsCount + 1);
    }

    @Test
    void canUnregisterActiveCars() {
        // given
        CarTypeDTO created = createCarClass(VAN, "description");
        // and
        registerActiveCar();
        // and
        int currentActiveCarsCount = load(created.getId()).getActiveCarsCounter();

        // when
        unregisterActiveCar(VAN);

        // then
        CarTypeDTO loaded = carTypeService.loadDto(created.getId());
        assertThat(loaded.getActiveCarsCounter()).isEqualTo(currentActiveCarsCount - 1);
    }

    private void thereIsNoCarClassInTheSystem(CarType.CarClass van) {
        carTypeService.removeCarType(van);
    }

    private CarTypeDTO createCarClass(CarType.CarClass carClass, String description) {
        CarTypeDTO carTypeDTO = new CarTypeDTO();
        carTypeDTO.setCarClass(carClass);
        carTypeDTO.setDescription(description);
        CarType carType = carTypeService.create(carTypeDTO);
        return carTypeService.loadDto(carType.getId());
    }

    private CarTypeDTO load(Long id) {
        return carTypeService.loadDto(id);
    }

    private void registerActiveCar() {
        carTypeService.registerActiveCar(VAN);
    }

    private void registerActiveCar(CarType.CarClass carClass) {
        carTypeService.registerActiveCar(carClass);
    }

    private void unregisterActiveCar(CarType.CarClass carClass) {
        carTypeService.unregisterActiveCar(carClass);
    }
}
