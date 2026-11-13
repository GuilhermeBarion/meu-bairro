# MeuBairro

**Sua cidade, sua participação.**

## Sobre o projeto

O MeuBairro é um aplicativo Android criado pela equipe **UrbanTech** como trabalho da faculdade. O tema é a **ODS 11 – Cidades e Comunidades Sustentáveis**, da ONU.

### Integrantes

- Guilherme Barion Ferreira
- Pedro Gabriel Lopes Da Cruz
- Caio Emanoel Xavier de Moraes

## Qual é a ideia?

Quase todo mundo já passou por um buraco na rua, um poste apagado ou um monte de entulho na calçada e não sabia onde registrar o problema. O MeuBairro serve para isso: o morador registra o problema do seu bairro e acompanha o andamento dele.

O app incentiva a participação das pessoas nas decisões da cidade, ajuda a chamar atenção para a acessibilidade dos espaços públicos e para os impactos ambientais, como lixo e alagamentos.

> **Importante:** o app **não envia denúncias automaticamente à prefeitura**. As ocorrências ficam registradas dentro do MeuBairro e são acompanhadas por administradores do próprio app. A integração com a prefeitura fica para versões futuras.

## Aviso sobre o envio de fotos

O código de foto está pronto: o app abre a câmera e a galeria, mostra a prévia, comprime a imagem e a envia para o **Firebase Storage**. Porém, o Firebase só libera o Cloud Storage em projetos com o **plano Blaze**, que é pago (exige cartão de crédito, com uma cota gratuita mensal). Na nossa configuração gratuita (plano Spark), o Storage não está ativo.

Por isso, **o envio de ocorrências com foto não funciona** neste projeto. Ao tentar enviar com foto, o app mostra a mensagem *"Falha ao enviar a foto"*. O resto do app funciona normalmente. A foto é opcional nas regras de negócio do trabalho, então registrar ocorrências **sem foto** funciona por completo: cadastro, listagem, filtros, detalhes, histórico e painel administrativo.

Quem tiver um projeto Firebase no plano Blaze só precisa ativar o Storage e publicar o arquivo `storage.rules`. Não é preciso mudar nenhuma linha de código.

## Como o app funciona

1. A pessoa cria uma conta com nome, e-mail e senha, e entra no app.
2. Na tela inicial, ela vê um resumo do que está acontecendo no bairro: indicadores, ocorrências recentes e as categorias de problemas.
3. Para registrar um problema, ela informa título, categoria, descrição e endereço. Pode digitar o endereço ou usar a localização do celular. Também pode anexar uma foto, tirada na hora ou escolhida da galeria. A foto é opcional.
4. Cada ocorrência nasce com o status **Recebida**. Ela fica visível na lista de ocorrências, onde dá para pesquisar e filtrar por categoria e por status.
5. Um **administrador** analisa a ocorrência e atualiza o status: *Em análise*, *Em andamento*, *Resolvida* ou *Não procedente*. Cada mudança fica guardada no histórico.
6. O morador acompanha tudo na tela **Minhas ocorrências**. Ele consegue ver o status, mas não alterá-lo.

As categorias disponíveis são: ruas e pavimentação, calçadas e acessibilidade, iluminação pública, limpeza urbana, praças e espaços públicos, e drenagem urbana.

### Telas do app

Splash, Login, Cadastro, Início, Registrar ocorrência, Ocorrências, Detalhes da ocorrência, Minhas ocorrências, Perfil e Painel administrativo. No Perfil dá para editar o nome, ver informações sobre o app, sair da conta e excluir a conta.

### Ficou para as próximas versões

Mapa interativo, notificações, comentários, curtidas/confirmações e integração com a prefeitura.

## Tecnologias

- Android nativo, com **Java** e layouts **XML**
- **Firebase Authentication**: cadastro e login
- **Cloud Firestore**: usuários e ocorrências
- **Firebase Storage**: fotos das ocorrências
- Serviços de localização do Google, para preencher o endereço
- Glide, para exibir as imagens

## Como rodar na sua máquina

Você vai precisar do **Android Studio** e de uma conta Google (para usar o Firebase gratuitamente).

### Passo 1: baixar o projeto

Clone o repositório ou baixe o ZIP e extraia:

```
git clone https://github.com/GuilhermeBarion/meu-bairro
```

### Passo 2: abrir no Android Studio

1. Abra o Android Studio e escolha **File → Open**.
2. Selecione a pasta do projeto (a que contém o arquivo `settings.gradle`).
3. Espere o Gradle sincronizar. Na primeira vez pode demorar alguns minutos, porque ele baixa as dependências.

> Se aparecer o erro *"Incompatible Gradle JVM version"*, vá em **File → Settings → Build, Execution, Deployment → Build Tools → Gradle** e, em **Gradle JDK**, escolha o **jbr-17** (ou o 21). Depois faça o sync de novo.

### Passo 3: criar o projeto no Firebase

O app precisa de um projeto Firebase para guardar os dados. Cada pessoa cria o seu:

1. Acesse https://console.firebase.google.com e clique em **Adicionar projeto**.
2. Dentro do projeto, clique em **Adicionar app → Android**.
3. No nome do pacote, escreva exatamente: `br.com.urbantech.meubairro`.
4. Baixe o arquivo **`google-services.json`** e coloque na pasta **`app/`** do projeto (ao lado do `app/build.gradle`). Sem esse arquivo o app não compila.
5. Você pode pular os passos do assistente que pedem para editar arquivos Gradle. O projeto já está configurado.

### Passo 4: ativar os serviços do Firebase

No console do Firebase:

1. **Authentication → Método de login → E-mail/senha**: ative.
2. **Firestore Database → Criar banco de dados**: escolha o modo de produção. Depois, na aba **Regras**, apague o conteúdo, cole o arquivo **`firestore.rules`** deste projeto e clique em **Publicar**.
3. **Storage** (opcional, exige o plano Blaze): clique em **Começar** e, na aba **Regras**, cole o conteúdo de **`storage.rules`** e publique.

> Se você estiver no plano gratuito, **pule o Storage**: veja o *Aviso sobre o envio de fotos* acima. O app funciona sem ele, apenas sem fotos.

### Passo 5: rodar o app

**No emulador:** abra o **Device Manager**, crie um dispositivo com Android 7.0 (API 24) ou mais novo e clique em **Run ▶**. Para testar o GPS, use *Extended controls → Location*.

**No celular:**
1. Ative as **Opções do desenvolvedor** (toque 7 vezes em "Número da versão" em *Sobre o telefone*).
2. Ative a **Depuração USB** e conecte o celular ao computador com um cabo que transfira dados.
3. Aceite o aviso de depuração no celular, escolha o aparelho na barra superior do Android Studio e clique em **Run ▶**.

### Passo 6: criar um administrador

Por segurança, o app não tem tela para virar administrador. Isso é feito direto no banco:

1. No app, crie uma conta e faça login nela uma vez.
2. No console do Firebase, abra **Firestore Database → Dados → coleção `usuarios`**.
3. Abra o documento com o e-mail dessa conta e mude o campo **`tipo`** de `usuario` para `administrador` (tudo minúsculo, sem espaços).
4. No app, saia da conta e entre de novo. Em **Perfil** vai aparecer o botão **Painel administrativo**.

## Segurança, em poucas palavras

- As senhas ficam no Firebase Authentication e nunca são salvas em texto puro.
- As regras do Firestore garantem que só administradores alteram o status das ocorrências. Isso vale mesmo que alguém tente burlar a interface.
- Um usuário comum só consegue criar ocorrências em seu próprio nome.
- O app só pede permissão de localização quando a pessoa toca em "Usar minha localização". Câmera e galeria usam o próprio sistema e não pedem permissão.

## Organização do código

O código fica em `app/src/main/java/br/com/urbantech/meubairro/`:

| Pasta | O que tem |
|---|---|
| `activities/` | Telas principais: Splash, Login, Cadastro, Main, Registrar, Detalhes e Admin |
| `fragments/` | Abas do menu inferior: Início, Lista de ocorrências e Perfil |
| `models/` | Classes de dados: Usuario, Ocorrencia e HistoricoStatus |
| `repositories/` | Acesso ao Firebase (autenticação, usuários e ocorrências) |
| `viewmodels/` | Estado das telas |
| `adapters/` | Adaptador da lista de ocorrências |
| `utils/` | Validações, mensagens de erro, imagens, diálogos e constantes |

## Limitações conhecidas

- As listas carregam até 200 ocorrências, e a pesquisa e os filtros são feitos no aparelho.
- O envio de fotos depende do Firebase Storage (plano pago). No plano gratuito, só é possível registrar ocorrências sem foto.
- Não há foto de perfil nem notificações nesta versão.
- Excluir a conta também apaga as ocorrências da pessoa (com histórico e foto) e pede a senha para confirmar.
