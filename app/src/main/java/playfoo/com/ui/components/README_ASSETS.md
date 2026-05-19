# Assets do Desenhista — Guia de Integração

## Como inserir um asset quando estiver pronto

1. Adicione o arquivo em `app/src/main/res/drawable/`
2. Referencie com `painterResource(R.drawable.nome_do_arquivo)`
3. Passe como parâmetro para o slot correspondente

## Mapa de Assets

### Botões (BotaoCartoon)
| Parâmetro | Arquivo esperado | Uso |
|---|---|---|
| assetPainter | btn_primario.png | Botão roxo principal |
| assetPainter | btn_secundario.png | Botão verde secundário |
| assetPainter | btn_perigo.png | Botão vermelho de perigo |
| assetPainter | btn_sucesso.png | Botão ciano de sucesso |

### Teclas (BotaoLetraCartoon)
| Parâmetro | Arquivo esperado | Uso |
|---|---|---|
| assetDisponivel | tecla_normal.png | Tecla ainda não usada |
| assetCorreta | tecla_correta.png | Tecla de letra correta |
| assetErrada | tecla_errada.png | Tecla de letra errada |

### Fundos de Tela (FundoTela)
| TipoFundo | Arquivo esperado | Tela |
|---|---|---|
| MENU | fundo_menu.png | Tela de menu principal |
| JOGO | fundo_jogo.png | Tela do jogo (1 personagem + PC) |
| MULTIPLAYER | fundo_multiplayer.png | Tela multiplayer (2 personagens + 2 PCs) |
| PERFIL | fundo_perfil.png | Tela de perfil |
| DASHBOARD | fundo_dashboard.png | Tela do gestor |
| TURMA | fundo_turma.png | Tela de turmas |

### Avatar (AvatarView)
| EstadoAvatar | Arquivo esperado | Situação |
|---|---|---|
| NEUTRO | avatar_neutro.png | Digitando normalmente |
| ACERTOU | avatar_acertou.png | Letra correta — feliz |
| ERROU | avatar_errou.png | Letra errada — brava |
| VITORIA | avatar_vitoria.png | Venceu — estrelas na cabeça |
| DERROTA | avatar_derrota.png | Perdeu — triste |

### Personalização do Avatar
| Combinação | Arquivo esperado | Exemplo |
|---|---|---|
| cabelo_curto_preto | cabelo_curto_preto.png | Cabelo curto cor preta |
| cabelo_longo_loiro | cabelo_longo_loiro.png | Cabelo longo cor loira |
| camisa_basica_azul | camisa_basica_azul.png | Camisa básica azul |
| (base por tom de pele) | avatar_base_{tom}.png | Base do corpo por tom de pele |

### Cards e UI
| Componente | Arquivo esperado | Uso |
|---|---|---|
| CardCartoon | card_fundo.png | Fundo dos cards |
| IconeCartoon | icone_{nome}.png | Ícones diversos |
