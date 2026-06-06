# PROTOCOLO RIGOROSO DE TESTES
A qualidade é inegociável. Siga este fluxo:

## 1. Regras Intocáveis
- **NEVER_DELETE:** Nunca remova testes, asserts, arquivos de mock ou fixtures sem a string de autorização explícita "AUTHORIZE_DELETE".
- Se um teste quebrar após sua alteração, a falha é da implementação, não do teste. Altere o teste APENAS se o requisito do sistema mudou oficialmente.

## 2. Estratégia de Cobertura
- **Unidade:** Isole a lógica de domínio. Use mocks/stubs estritos para dependências externas (bancos de dados, chamadas de rede).
- **Integração:** Valide as fronteiras de I/O. Verifique se o banco de dados processa a transação e se os endpoints retornam o payload com a tipagem correta.
- **Edge Cases:** Crie obrigatoriamente cenários para entradas nulas, vazias, limites máximos numéricos e falhas de timeout em integrações.

Ao entregar o código, forneça imediatamente o comando de terminal para a execução isolada deste teste específico.