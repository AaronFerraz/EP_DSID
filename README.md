## EACHare - Sistema de Compartilhamento de Arquivos P2P
Este projeto é uma implementação de um sistema de compartilhamento de arquivos peer-to-peer (P2P) simplificado, desenvolvido para a disciplina de ACH2147 - Desenvolvimento de Sistemas de Informação Distribuídos. O sistema permite que múltiplos "peers" (nós) se conectem em rede, descubram uns aos outros, compartilhem arquivos e façam downloads de forma paralela e fragmentada.

- [Descrição](#descrição)
- [Funcionalidades Principais](#funcionalidades-principais)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Instalação e Execução](#instalação-e-execução)

<br>

### Descrição
O objetivo central do EACHare é simular uma rede de compartilhamento de arquivos onde não existe um servidor central. Cada nó (peer) na rede atua simultaneamente como cliente e como servidor , sendo responsável por manter sua própria lista de peers conhecidos e disponibilizar um diretório local de arquivos para compartilhamento.

O sistema é controlado por um menu de texto e utiliza um protocolo de comunicação customizado, baseado em texto puro sobre TCP. Para garantir a consistência das informações de status (ONLINE/OFFLINE) dos peers na rede, o sistema implementa um Relógio de Lamport.

Uma das principais características do projeto é a capacidade de realizar downloads de forma paralela. Ao buscar um arquivo, o sistema identifica todos os peers que o possuem. O download é então dividido em "chunks" (pedaços), que são requisitados de múltiplos peers simultaneamente, otimizando a transferência.

<br>

### Funcionalidades Principais
O menu principal oferece as seguintes operações:
- [1] Listar peers: Exibe todos os peers conhecidos e seus respectivos status (ONLINE ou OFFLINE). Permite enviar uma mensagem HELLO para atualizar o status de um peer.
- [2] Obter peers: Envia uma mensagem GET_PEERS para todos os peers conhecidos, descobrindo novos nós na rede e atualizando a lista local com base nas respostas PEER_LIST.
- [3] Listar arquivos locais: Mostra os arquivos presentes no diretório de compartilhamento local.
- [4] Buscar arquivos:
  -  Envia mensagens LS para todos os peers ONLINE para descobrir seus arquivos.
  -  Compila uma lista única, agrupando arquivos idênticos (mesmo nome e tamanho) e     listando todos os peers que os possuem.
  -  Permite ao usuário escolher um arquivo para download.
  -  Inicia o download paralelo, enviando mensagens DL para os peers relevantes, solicitando "chunks" específicos do arquivo .
  -  Remonta os chunks recebidos (codificados em Base64) e salva o arquivo final no diretório local.
- [5] Exibir estatísticas: Calcula e exibe o tempo médio e o desvio padrão dos downloads, agrupados por tamanho do chunk, tamanho do arquivo e número de peers envolvidos no download.
- [6] Alterar tamanho de chunk: Permite ao usuário definir o tamanho (em bytes) de cada "chunk" a ser usado nos downloads. O valor padrão na inicialização é 256 bytes.
- [9] Sair: Envia uma mensagem BYE para todos os peers ONLINE , informa a rede da sua saída e encerra o programa.

<br>

### Tecnologias Utilizadas
Java (JDK 21):
- java.net.Socket e java.net.ServerSocket: Utilizados para estabelecer a comunicação TCP entre os peers, permitindo que cada nó atue como um servidor.
- java.lang.Thread (ou Runnable): Essencial para a programação paralela. Cada conexão de entrada é tratada em uma thread separada (PeerHandler), permitindo que o peer sirva múltiplos outros peers simultaneamente e, ao mesmo tempo, mantenha seu menu interativo para o usuário.
- java.util.Base64: Usado para codificar (no envio) e decodificar (no recebimento) o conteúdo dos arquivos durante o download , garantindo que os dados binários sejam transmitidos de forma segura como texto dentro do protocolo.

<br>

### Estrutura do Projeto
A estrutura de pastas e arquivos foi organizada para separar responsabilidades, como o protocolo de comunicação, a lógica dos peers e o logging.
```
└───src
    │   Eachare.java
    │   EP1.zip
    │   Relatorio-DSID.pdf
    │
    ├───logger
    │       Logger.java
    │       LoggerFactory.java
    │
    │       Peer.java
    │       PeerFile.java
    │       PeerHandler.java
    │       PeerInfo.java
    │       PeerStats.java
    │
    └───Protocol
            MessageHandler.java
            MessageHelper.java
```
<br>

### Instalação e Execução
1. Clone o repositório:
2. Compile o projeto. Supondo que você esteja na raiz do repositório, o comando a seguir compilará todos os arquivos .java para um diretório bin:
3. Execute o programa. Você precisará de pelo menos um terminal para cada peer.
A sintaxe de execução é:
- ``<endereco>:<porta>``: O endereço IP e a porta que este peer usará para escutar (ex: 127.0.0.1:9001).
- ``<vizinhos.txt>``: Um arquivo de texto contendo a lista inicial de peers conhecidos (ex: vizinhos_peer1.txt). O formato é um <ip>:<porta> por linha.
- ``<diretorio_compartilhado>``: O nome da pasta local que contém os arquivos a serem compartilhados (ex: arquivos_peer1/).

Exemplo de Inicialização (2 peers locais):
- Crie seus arquivos de vizinhos e diretórios de arquivos:
  - vizinhos1.txt (contendo 127.0.0.1:9002)
  - vizinhos2.txt (contendo 127.0.0.1:9001)
  - arquivos1/ (contendo teste.txt)
  - arquivos2/ (contendo outro.pdf)
- Terminal 1 (Peer 1):
- Terminal 2 (Peer 2):

Agora você pode usar o menu em qualquer um dos terminais para interagir com a rede.
