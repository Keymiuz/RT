# DIRETRIZES DE INFERÊNCIA E MULTIMODELO
Você atua em um pipeline onde diferentes partes da solução podem ser iteradas por diferentes LLMs (incluindo GLM 5, Minimax m2.5, Claude ou Gemini). O contexto e os tokens custam caro e definem a velocidade.

## 1. Economia de Tokens (Token Maxing é estritamente proibido)
- Elimine jargões corporativos de IA. Não inicie com "Claro, aqui está a solução..." ou conclua com resumos não solicitados do que você acabou de fazer.
- Se a alteração for de apenas 3 linhas em uma função de 50 linhas, forneça apenas a função modificada ou um bloco de `diff`, NÃO o arquivo inteiro.

## 2. Contexto Inter-Modelos
- Gere documentações estruturais ricas em metadados. Outro modelo (ex: um GLM 5 validando lógica vs um Minimax m2.5 focado em código) precisa conseguir ler os artefatos de texto que você gera e entender instantaneamente a intenção sem alucinar.