#uRPG

uRPG é um RPG tático com batalhas em turnos, construido com base no [middleware uOS] (https://github.com/UnBiquitous/uos_core) e na [engine uImpala] (https://github.com/matheuscscp/uImpala). Você pode encontrar vídeos e screenshots na [Wiki do projeto] (https://github.com/LBNunes/uRPG/wiki).

##História

Um grupo de aventureiros viaja pelo mundo buscando missões em diversas cidades. Ou, mais importante: Buscando recompensas. Para isso, tentam ser guerreiros cada vez mais fortes e melhor equipados, para buscar gratificações cada vez maiores.

##Estética

Trata-se de um jogo 2D. A maior parte dos sprites usados são apenas placeholders. Imagens de fontes da internet usados estão listados no arquivo Game/img/credits.txt.

##Mecânica

A aplicação existe em dois perfis: Uma quando é executada é executada num laptop, e outra quando num PC. O jogo em si acontece no primeiro perfil: O jogador começa com três personagens na equipe, um de cada uma das classes básicas. 

A tela principal é o world map, contendo um conjunto de áreas que podem ser visitadas. O jogo usa o SSID da rede em que o jogador está conectado para determinar quais áreas (tais como floresta, deserto, lago, e outras) aparecem naquela região, a qual recebe como nome o próprio SSID. Quando o jogador seleciona uma área, uma batalha com um grupo de monstros se inicia, grupo esse determinado pela área em si, pelo Job Level dos membros da equipe, e pela luminosidade do ambiente em que o jogador se encontra (que determina se é dia ou noite dentro do jogo).

A batalha ocorre numa grade hexagonal, com cada personagem e inimigo tendo seu turno. Durante o turno, cada personagem pode executar uma ação dentre atacar, usar uma habilidade, ou usar um item. Quando todos os inimigos são derrotados, o jogador recebe espólios, que podem ser usados para criar equipamentos melhores para sua equipe. Ele não pode criá-los por conta própria, no entanto: ele depende de uma cidade para fazê-los.

Daí entra o segundo perfil da aplicação, o de uma cidade. Um PC na rede rodando a aplicação neste perfil adiciona uma nova área ao mapa da região, uma cidade com o nome do PC. Ao selecionar essa cidade, o jogador é levado a um outro menu, onde pode selecionar áreas da cidade para realizar tarefas impossíveis no jogo solo.

A primeira área seria a forja, onde o jogador pode usar os itens obtidos em seu inventário para forjar novos equipamentos. Isso é de extrema importância, porque ainda que haja uma mecânica de Job Level, os stats do personagem não melhoram com esses níveis. É necessário manter uma equipe bem equipada para suceder em batalhas mais difíceis.

No entanto, apesar de ser possível conseguir todos os componentes por conta própria, pode ser que um jogador não consiga encontrar um item faltante, ou não precise de um que possui. Nesse caso, a cidade oferece um mercado, onde jogadores podem colocar itens à venda e comprar itens de outros jogadores. Não é necessário que os dois jogadores estejam na cidade ao mesmo tempo; ao completar a transação, o comprador recebe o item, e a cidade espera o vendedor voltar à tela do market para entregá-lo o pagamento.

Também existe uma guilda, onde a cidade, a cada poucas horas, disponibiliza missões aos jogadores, que incluem matar um determinado tipo de monstro, entregar um item, visitar uma área do mapa e visitar uma outra cidade. são a fonte de renda principal dos jogadores, já que as quantias de dinheiro recebido em batalhas são insignificantes. Deve-se observar, no entanto, que após aceitar uma missão, o jogador deve esperar alguns minutos antes de poder aceitar outra. Além disso, missões aceitas por um jogador ainda podem ser aceitas e completadas por outros, caso em que o primeiro a voltar à guilda para receber a recompensa será o único recompensado pela missão.

A última área da cidade é a academia. Durante a criação da cidade, uma das classes básicas é escolhida aleatoriamente, e a academia passará a oferecer personagens daquela classe para serem recrutados por jogadores. Novos recrutas são gerados juntamente com as missões, e podem ter níveis tão altos quanto os dos personagens de nível mais alto que já visitaram aquela cidade.

Além disso, na academia, personagens da classe com Job Level maior ou igual a 8 podem escolher uma de duas promoções, novas classes que podem equipamentos e habilidades melhores, exclusivos a elas. Promover um personagem também aumenta os seus stats, logo, o investimento pode valer a pena. Por último, na academia, personagens podem comprar habilidades, contanto que tenham o nível requerido para usá-las.

Uma cidade não compartilha dados com outras, logo, existe uma motivação para o jogador tentar visitar várias cidades, tanto para melhorar diferentes classes, como para aceitar mais missões, como para buscar itens que podem estar à venda nas mesmas. 

O jogo se dá em ambientes fechados onde há uma rede, com PCs e laptops conectados. Os laptops devem dispor de webcam.

A aplicação oferece os seguintes recursos e serviços:

- Em um PC
  - Geração de missões e personagens novos
  - Destravar habilidades e promoções
  - Compra e venda de itens entre jogadores
  - Criação de novos itens

- Em um laptop
  - Geração de regiões a partir de SSIDs
  - Geração de batalhas e espólios
  - Checagem de missões completadas
  - Checagem de dia e noite via webcam
