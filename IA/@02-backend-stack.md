# PADRÕES ESTRITOS DE BACKEND E SISTEMAS
Stack: Java, C, C++.

## 1. C e C++ (Baixo Nível e Performance)
- **Gestão de Memória:** O escopo define a vida útil. Toda alocação dinâmica (`malloc`, `calloc`, `new`) deve ter seu ponto de liberação (`free`, `delete`) explicitamente documentado no mesmo fluxo léxico. 
- **Ponteiros:** Valide ponteiros contra `NULL` antes de qualquer desreferenciamento. Prefira "smart pointers" (`std::unique_ptr`, `std::shared_ptr`) em C++ moderno para evitar memory leaks.
- **Segurança:** Evite funções de string inseguras da libc (`strcpy`, `sprintf`). Use variantes com limite de buffer (`strncpy`, `snprintf`).

## 2. Java
- **Imutabilidade e Concorrência:** Estruturas de dados devem ser imutáveis por padrão. Utilize `Records` para DTOs.
- **Tratamento de Exceções:** Proibido capturar blocos genéricos (`catch (Exception e)`). Capture apenas exceções específicas. Exceções não checadas (*Unchecked*) devem ser usadas para falhas de programação, e checadas (*Checked*) para falhas recuperáveis do ambiente (I/O, Rede).

## 3. APIs RESTful
- Respostas HTTP devem ser padronizadas (ex: envelopadas em `{ data: ..., error: ... }`).
- Status codes devem ser semânticos (ex: 201 para criação, 400 para erro de validação de domínio, 404 para entidade não encontrada).