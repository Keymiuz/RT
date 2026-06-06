# PADRÕES ESTRITOS DE FRONTEND
Stack: React, Next.js, Angular, TypeScript.

## 1. TypeScript & Tipagem
- `any` é um erro fatal. Use `unknown` se a resposta da API for imprevisível, validando-a em runtime com Type Guards (ex: Zod).
- Exporte interfaces explícitas para TODAS as props de componentes e retornos de funções.

## 2. Next.js & React
- **Client vs Server:** Todo componente Next.js é um Server Component por padrão. Use `"use client"` apenas nas folhas extremas da árvore de renderização que exigem estado ou ciclo de vida (ex: botões, inputs).
- **Hooks:** Evite correntes de `useEffect`. Se um estado deriva de outro, calcule-o durante a renderização, sem usar hooks de efeito.
- **Gerenciamento de Estado:** Mantenha o estado o mais próximo possível de onde ele é usado. Evite estado global a menos que seja para dados persistentes de sessão.

## 3. Angular
- Utilize o padrão de Arquitetura Baseada em Serviços. Injeção de dependência deve ser feita no construtor.
- Adote `Signals` para reatividade local quando possível, ou `RxJS` rigoroso para fluxos assíncronos complexos, sempre garantindo o "unsubscribe" ou usando o pipe `async` no template para evitar vazamento de memória.