package io.legacyfighter.cabs.contracts.application.straightforward;

import io.legacyfighter.cabs.contracts.model.ContractHeader;
import io.legacyfighter.cabs.contracts.model.state.straightforward.DraftState;
import io.legacyfighter.cabs.document.model.state.straightforward.BaseState;
import org.springframework.stereotype.Component;

@Component
public class ContractStateFactory {

    @SuppressWarnings("unchecked")
    public <T extends BaseState> BaseState create(ContractHeader header) {
        // sample impl is based on class names
        // other possibilities: names Dependency Injection Containers, states persisted via ORM Discriminator mechanism, mapper
        String className = header.getStateDescriptor();

        if (className == null) {
            DraftState state = new DraftState();
            state.init(header);
            return state;
        }

        try {
            Class<T> clazz = (Class<T>) Class.forName(className);
            BaseState state = clazz.getConstructor().newInstance();
            state.init(header);
            return state;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
