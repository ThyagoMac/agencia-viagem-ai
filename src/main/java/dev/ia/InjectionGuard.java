package dev.ia;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InjectionGuard implements InputGuardrail {
  @Inject
  PromptSecurityExpert promptSecurityExpert;

  @Override
  public InputGuardrailResult validate(UserMessage userMessage) {
    if (promptSecurityExpert.isAttack(userMessage.singleText())) {
      return failure("Prompt bloqueado por ser malicioso ou inseguro");
    }
    return InputGuardrailResult.success();
  }
}
