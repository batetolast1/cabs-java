package io.legacyfighter.cabs.document.model.state.dynamic;

import io.legacyfighter.cabs.document.model.DocumentHeader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Component
public class DynamicStateAssembler<T extends DocumentHeader> {

    private final StateDefinitionRepository<T> stateDefinitionRepository;

    private final ApplicationContext applicationContext;

    public DynamicStateAssembler(StateDefinitionRepository<T> stateDefinitionRepository,
                                 ApplicationContext applicationContext) {
        this.stateDefinitionRepository = stateDefinitionRepository;
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public StateConfig<T> assemble(Class<T> clazz) {
        StateBuilder<T> builder = new StateBuilder<>();

        List<StateDefinition> stateDefinitions = stateDefinitionRepository.findAll(clazz);
        stateDefinitions.forEach(stateConfig -> {
            if (stateConfig.isInitialState()) {
                builder.beginWith(stateConfig.getStateFrom());
            } else {
                builder.from(stateConfig.getStateFrom());
            }

            if (stateConfig.isWhenContentChanged()) {
                builder.whenContentChanged();
            }

            stateConfig.getChecks().forEach(c -> {
                try {
                    Class<? extends BiPredicate<State, ChangeCommand>> checkClazz = (Class<BiPredicate<State, ChangeCommand>>) Class.forName(c.getCheckClassName());
                    BiPredicate<State, ChangeCommand> check = checkClazz.getConstructor().newInstance();
                    builder.check(check);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            });

            if (stateConfig.getAction() == null) {
                builder.to(stateConfig.getStateTo());
            } else {
                try {
                    Class<? extends BiConsumer<DocumentHeader, ChangeCommand>> actionClazz = (Class<BiConsumer<DocumentHeader, ChangeCommand>>) Class.forName(stateConfig.getAction().getActionClassName());

                    List<Class<?>> parameterClazzes = stateConfig.getAction().getActionParameterClassNames().stream()
                            .map(parameter -> {
                                try {
                                    return Class.forName(parameter);
                                } catch (Exception e) {
                                    throw new IllegalArgumentException(e);
                                }
                            })
                            .collect(Collectors.toList());

                    Object[] parameterObjects = parameterClazzes.stream()
                            .map(parameterClazz -> {
                                if (applicationContext.containsBean(Character.toLowerCase(parameterClazz.getSimpleName().charAt(0)) + parameterClazz.getSimpleName().substring(1))) {
                                    return applicationContext.getBean(parameterClazz);
                                } else if (parameterClazz.isAssignableFrom(org.springframework.context.ApplicationEventPublisher.class)) {
                                    return applicationContext;
                                } else {
                                    try {
                                        if (actionClazz.getConstructors()[0].getParameterTypes()[parameterClazzes.indexOf(parameterClazz)].getName().equals("java.lang.Class")) {
                                            return parameterClazz;
                                        }
                                        return parameterClazz.getConstructor().newInstance();
                                    } catch (Exception e) {
                                        throw new IllegalArgumentException(e);
                                    }
                                }
                            })
                            .toArray();

                    BiConsumer<DocumentHeader, ChangeCommand> action = (BiConsumer<DocumentHeader, ChangeCommand>) actionClazz.getConstructors()[0].newInstance(parameterObjects);

                    builder.to(stateConfig.getStateTo()).action(action);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        return builder;
    }
}
