// Random elige valores al azar y Scanner lee los comandos de la consola.
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class TableroPuntos {
    static final Random AZAR = new Random();
    static final char VACIA = '.', MURO = '#', ENEMIGO = 'X', ELITE = 'F';
    static final char TRAMPA = 'T', TRAMPA_AVANZADA = 'V', COFRE = 'C', CURACION = '+';
    static final char COMERCIANTE = 'M', ACERTIJO = 'A', LLAVE = 'L', TESORO = 'S';
    // Cada posición de estos arreglos representa una dirección: arriba, abajo, izquierda o derecha.
    static final int[] DELTA_FILA = {-1, 1, 0, 0};
    static final int[] DELTA_COLUMNA = {0, 0, -1, 1};

    static int vidasIniciales = 5;
    static int tamano = 10;
    static int dificultad = 1;
    static final String[] DIFICULTADES = {"Fácil", "Normal", "Difícil"};

    // Estos datos pertenecen a una partida. La configuración se conserva en memoria.
    static char[][] tablero;
    static int fila, columna, vidas, oro, pociones, veneno, escudo;
    static boolean tieneLlave, tesoroDescubierto, victoria;

    static final String[] PREGUNTAS = {
        "¿Cuánto es 7 por 6?\n1. 36  2. 42  3. 48",
        "¿Qué número sigue? 2, 4, 8, 16...\n1. 18  2. 24  3. 32",
        "¿Cuántos lados tiene un hexágono?\n1. 6  2. 7  3. 8"
    };
    static final int[] RESPUESTAS = {2, 3, 1};

    public static void main(String[] args) {
        System.out.println("¡Bienvenido, profe Víctor! Busca la Llave Mística y el Tesoro Supremo.");
        try (Scanner entrada = new Scanner(System.in)) {
            menuPrincipal(entrada);
        } catch (java.util.NoSuchElementException e) {
            // El fin de la entrada cierra el programa incluso dentro de un submenú.
            System.out.println("\nEntrada cerrada. Fin del juego.");
        }
    }

    // Repite el menú principal al terminar una partida o al volver de un submenú.
    static void menuPrincipal(Scanner entrada) {
        while (true) {
            System.out.println("\n=== LA MAZMORRA INFINITA ===");
            System.out.println("1. Nueva Partida\n2. Configuración\n3. Instrucciones\n4. Créditos\n5. Salir");
            switch (leerOpcion(entrada, 1, 5)) {
                case 1: jugarSesion(entrada); break;
                case 2: configurar(entrada); break;
                case 3: mostrarInstrucciones(); break;
                case 4: System.out.println("La Mazmorra Infinita – Aventura Procedural\nAutor: José Manuel"); break;
                case 5: return;
            }
        }
    }

    // Actualiza las opciones que se aplicarán al iniciar la siguiente partida.
    static void configurar(Scanner entrada) {
        while (true) {
            System.out.printf("%n=== CONFIGURACIÓN ===%n1. Vidas (%d)%n2. Mapa (%d × %d)%n"
                    + "3. Dificultad (%s)%n4. Volver%n",
                    vidasIniciales, tamano, tamano, DIFICULTADES[dificultad - 1]);
            switch (leerOpcion(entrada, 1, 4)) {
                case 1:
                    System.out.println("Elige 3, 4 o 5 vidas.");
                    vidasIniciales = leerOpcion(entrada, 3, 5);
                    break;
                case 2:
                    System.out.println("1. 6 × 6  2. 8 × 8  3. 10 × 10");
                    tamano = 4 + 2 * leerOpcion(entrada, 1, 3);
                    break;
                case 3:
                    System.out.println("1. Fácil  2. Normal  3. Difícil");
                    dificultad = leerOpcion(entrada, 1, 3);
                    break;
                case 4: return;
            }
        }
    }

    static void mostrarInstrucciones() {
        mostrarObjetivo();
        System.out.println("P es el jugador, . es suelo libre y # es una pared. El tesoro está oculto.");
        System.out.println("Usa W, S, A y D o las opciones numeradas para moverte.");
        System.out.println("En los duelos, elige 1 para piedra, 2 para papel o 3 para tijera.");
        System.out.println("Los encuentros, las trampas y los cofres se activan al entrar en su casilla.");
        System.out.println("Los comerciantes venden pociones, escudos y pistas a cambio de oro.");
        System.out.println("Moverse o beber una poción consume un turno. Consultar el estado es gratis.");
        System.out.println("El veneno quita una vida por turno. El escudo reduce el daño de los enemigos.");
        System.out.println("En dificultad Fácil empiezas con una poción. Usa la opción 6 para beberla.");
    }

    // Explica cómo se gana antes de empezar y desde el menú de instrucciones.
    static void mostrarObjetivo() {
        System.out.println("\n=== CÓMO GANAR ===");
        System.out.println("1. Recoge la Llave Mística entrando en la casilla L.");
        System.out.println("2. Explora el mapa para encontrar el Tesoro Supremo. Está oculto en una casilla.");
        System.out.println("3. Entra en la casilla del tesoro con la llave y al menos una vida.");
        System.out.println("Si te cuesta encontrarlo, un comerciante (M) vende pistas por 2 de oro.");
    }

    // Captura texto que no se puede convertir y opciones numéricas fuera del menú.
    static int leerOpcion(Scanner entrada, int minimo, int maximo) {
        return leerOpcion(entrada, minimo, maximo, false);
    }

    // Durante la partida también acepta las teclas del juego anterior.
    static int leerOpcion(Scanner entrada, int minimo, int maximo, boolean aceptarAtajos) {
        while (true) {
            System.out.print("Seleccione una opción: ");
            try {
                String texto = entrada.nextLine().trim();
                if (aceptarAtajos && texto.length() == 1) {
                    // El índice del atajo corresponde al número de la acción menos uno.
                    int indice = "wsad  qh".indexOf(Character.toLowerCase(texto.charAt(0)));
                    if (indice >= 0) {
                        return indice + 1;
                    }
                }
                int opcion = Integer.parseInt(texto);
                if (opcion < minimo || opcion > maximo) {
                    throw new IllegalArgumentException("Elige un número entre " + minimo + " y " + maximo + ".");
                }
                return opcion;
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número entero.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Genera otro tablero si el jugador elige repetir con la misma configuración.
    static void jugarSesion(Scanner entrada) {
        do {
            iniciarPartida();
            mostrarObjetivo();
            jugarPartida(entrada);
            System.out.println("\n¿Desea jugar otra vez? 1. Sí  2. No");
        } while (leerOpcion(entrada, 1, 2) == 1);
    }

    // Reinicia el inventario, las vidas, los efectos y el progreso de la aventura.
    static void iniciarPartida() {
        generarMapa();
        vidas = vidasIniciales;
        oro = veneno = escudo = 0;
        // La dificultad Fácil da una poción desde el inicio.
        pociones = dificultad == 1 ? 1 : 0;
        tieneLlave = tesoroDescubierto = victoria = false;
    }

    // Baraja las coordenadas para repartir eventos sin que dos ocupen la misma casilla.
    // Repite la generación si los muros aíslan alguna casilla transitable.
    static void generarMapa() {
        int escala = (tamano - 6) / 2;
        List<Integer> casillas = new ArrayList<>();
        for (int i = 0; i < tamano * tamano; i++) {
            casillas.add(i);
        }

        do {
            tablero = new char[tamano][tamano];
            for (char[] linea : tablero) {
                Arrays.fill(linea, VACIA);
            }
            Collections.shuffle(casillas, AZAR);
            int inicio = casillas.get(0);
            fila = inicio / tamano;
            columna = inicio % tamano;

            // Reserva la primera coordenada para P y ajusta las cantidades al tamaño y la dificultad.
            int siguiente = 1;
            siguiente = colocar(casillas, siguiente, MURO, 3 + escala * 2 + dificultad);
            siguiente = colocar(casillas, siguiente, ENEMIGO, dificultad == 1 ? 1 : 1 + dificultad + escala);
            siguiente = colocar(casillas, siguiente, ELITE, dificultad == 3 ? 2 : 1);
            siguiente = colocar(casillas, siguiente, TRAMPA, dificultad == 1 ? 1 : dificultad + escala);
            siguiente = colocar(casillas, siguiente, TRAMPA_AVANZADA, dificultad == 3 ? 2 : 1);
            siguiente = colocar(casillas, siguiente, COFRE, 4 - dificultad + escala);
            siguiente = colocar(casillas, siguiente, CURACION, 4 - dificultad + escala);
            siguiente = colocar(casillas, siguiente, COMERCIANTE, 1 + escala);
            siguiente = colocar(casillas, siguiente, ACERTIJO, 2 + escala);
            siguiente = colocar(casillas, siguiente, LLAVE, 1);
            colocar(casillas, siguiente, TESORO, 1);
        } while (!mapaConectado());
    }

    // Convierte cada posición de la lista en una fila y una columna de la matriz.
    static int colocar(List<Integer> casillas, int siguiente, char evento, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            int casilla = casillas.get(siguiente++);
            tablero[casilla / tamano][casilla % tamano] = evento;
        }
        return siguiente;
    }

    // Comprueba que se pueda llegar a todas las casillas que no sean muros.
    static boolean mapaConectado() {
        boolean[][] alcanzadas = new boolean[tamano][tamano];
        marcarCasillas(fila, columna, alcanzadas);
        for (int f = 0; f < tamano; f++) {
            for (int c = 0; c < tamano; c++) {
                if (tablero[f][c] != MURO && !alcanzadas[f][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    // Marca una casilla y repite la comprobación con sus vecinos.
    // El tesoro se marca como accesible, pero no sirve de paso hacia la llave.
    static void marcarCasillas(int f, int c, boolean[][] alcanzadas) {
        if (!dentro(f, c) || alcanzadas[f][c] || tablero[f][c] == MURO) {
            return;
        }
        alcanzadas[f][c] = true;
        if (tablero[f][c] != TESORO) {
            for (int d = 0; d < 4; d++) {
                marcarCasillas(f + DELTA_FILA[d], c + DELTA_COLUMNA[d], alcanzadas);
            }
        }
    }

    static boolean dentro(int f, int c) {
        return f >= 0 && f < tamano && c >= 0 && c < tamano;
    }

    // Dibuja los símbolos del tablero y oculta la casilla del tesoro hasta descubrirla.
    static void mostrarTablero() {
        System.out.println();
        for (int f = 0; f < tamano; f++) {
            for (int c = 0; c < tamano; c++) {
                char simbolo = tablero[f][c];
                if (f == fila && c == columna) {
                    simbolo = 'P';
                } else if (simbolo == TESORO && !tesoroDescubierto) {
                    simbolo = VACIA;
                }
                System.out.print(simbolo + " ");
            }
            System.out.println();
        }
    }

    // Procesa los comandos hasta que el jugador gana, pierde todas las vidas o abandona.
    static void jugarPartida(Scanner entrada) {
        while (vidas > 0 && !victoria) {
            mostrarTablero();
            System.out.printf("Vidas: %d/%d | Oro: %d | Pociones: %d | Llave: %s%n",
                    vidas, vidasIniciales, oro, pociones, tieneLlave ? "Sí" : "No");
            System.out.println("X enemigo | F élite | T trampa | V trampa avanzada | C cofre");
            System.out.println("+ curación | M comerciante | A acertijo | L llave | # pared | S tesoro descubierto");
            System.out.println("\n=== ACCIONES ===\n1. Mover arriba\n2. Mover abajo\n3. Mover izquierda"
                    + "\n4. Mover derecha\n5. Ver estado del jugador\n6. Usar objeto/curación\n7. Abandonar partida");
            System.out.println("Atajos: W S A D para moverte | H para recordar el objetivo | Q para abandonar");
            int opcion = leerOpcion(entrada, 1, 7, true);
            if (opcion == 7) {
                System.out.println("Has abandonado la partida.");
                mostrarEstado();
                return;
            }
            try {
                if (opcion <= 4) {
                    mover(opcion - 1, entrada);
                } else if (opcion == 5) {
                    mostrarEstado();
                } else if (opcion == 6) {
                    usarPocion();
                    avanzarTurno();
                } else {
                    System.out.println(tieneLlave
                            ? "Objetivo: encuentra el Tesoro Supremo. Un comerciante puede darte su ubicación."
                            : "Objetivo: recoge la Llave Mística (L).");
                }
            } catch (IllegalStateException e) {
                // Un movimiento bloqueado o un objeto no disponible no consumen un turno.
                System.out.println(e.getMessage());
            }
        }

        if (victoria) {
            System.out.println("¡Victoria! Has encontrado el Tesoro Supremo.");
        } else {
            System.out.println("Derrota. Te has quedado sin vidas.");
        }
        mostrarEstado();
    }

    static void mostrarEstado() {
        System.out.printf("Vidas: %d/%d | Oro: %d | Pociones: %d | Llave Mística: %s%n",
                vidas, vidasIniciales, oro, pociones, tieneLlave ? "Sí" : "No");
        System.out.printf("Veneno: %d turnos | Escudo: %d movimientos%n", veneno, escudo);
    }

    static void mover(int direccion, Scanner entrada) {
        int f = fila + DELTA_FILA[direccion];
        int c = columna + DELTA_COLUMNA[direccion];
        // Valida los índices antes de acceder a la matriz y señala los bloqueos con throw.
        if (!dentro(f, c)) {
            throw new IllegalStateException("No puedes salir del mapa.");
        }
        if (tablero[f][c] == MURO) {
            throw new IllegalStateException("Un muro bloquea el paso.");
        }
        // Revela el símbolo del tesoro si el jugador lo encuentra con la puerta cerrada.
        if (tablero[f][c] == TESORO && !tieneLlave) {
            tesoroDescubierto = true;
            throw new IllegalStateException("Has encontrado el tesoro. Necesitas la Llave Mística para abrirlo.");
        }

        fila = f;
        columna = c;
        avanzarTurno();
        if (vidas > 0) {
            // El escudo protege este movimiento. Una mejora recién comprada empieza en el siguiente.
            boolean protegido = escudo > 0;
            if (protegido) {
                escudo--;
            }
            procesarEvento(entrada, protegido);
        }
    }

    // Aplica un punto de daño y reduce la duración del veneno al gastar un turno.
    static void avanzarTurno() {
        if (veneno > 0) {
            veneno--;
            recibirDano(1, "El veneno");
        }
    }

    // Comprueba el inventario y las vidas antes de gastar una poción.
    static void usarPocion() {
        if (pociones == 0) {
            throw new IllegalStateException("No tienes pociones.");
        }
        if (vidas == vidasIniciales) {
            throw new IllegalStateException("Ya tienes el máximo de vidas.");
        }
        pociones--;
        curar(2);
    }

    // El símbolo de la casilla decide qué evento se ejecuta. El comerciante permanece
    // disponible para visitas futuras. El resto de encuentros se consume al activarse.
    static void procesarEvento(Scanner entrada, boolean protegido) {
        char evento = tablero[fila][columna];
        switch (evento) {
            case ENEMIGO: combatir(entrada, false, protegido); break;
            case ELITE: combatir(entrada, true, protegido); break;
            case TRAMPA: recibirDano(1, "La trampa"); break;
            case TRAMPA_AVANZADA: activarTrampaAvanzada(); break;
            case CURACION: curar(2); break;
            case COFRE: abrirCofre(); break;
            case COMERCIANTE: comerciar(entrada); return;
            case ACERTIJO: resolverAcertijo(entrada); break;
            case LLAVE:
                tieneLlave = true;
                System.out.println("Has encontrado la Llave Mística.");
                break;
            case TESORO:
                victoria = true;
                break;
            default: return;
        }
        tablero[fila][columna] = VACIA;
    }

    // Un duelo ganado da oro. Una derrota aplica el daño del tipo de enemigo.
    static void combatir(Scanner entrada, boolean elite, boolean protegido) {
        String enemigo = elite ? "El enemigo élite" : "El enemigo común";
        System.out.println(enemigo + " te desafía a piedra, papel o tijera.");
        if (ganarDuelo(entrada)) {
            System.out.println("¡Has ganado el duelo!");
            ganarOro(elite ? 6 : 3);
            return;
        }

        int dano = elite ? 2 : 1;
        if (elite && AZAR.nextInt(4) == 0) {
            dano++;
            System.out.println("¡Ataque crítico del enemigo élite!");
        }
        if (protegido) {
            dano = Math.max(0, dano - 1);
            System.out.println("El escudo reduce el daño del ataque.");
        }
        recibirDano(dano, enemigo);
    }

    // Repite los empates y comprueba las tres combinaciones ganadoras.
    static boolean ganarDuelo(Scanner entrada) {
        String[] jugadas = {"", "Piedra", "Papel", "Tijera"};
        while (true) {
            System.out.println("1. Piedra  2. Papel  3. Tijera");
            int jugador = leerOpcion(entrada, 1, 3);
            int rival = AZAR.nextInt(3) + 1;
            System.out.println("Tú: " + jugadas[jugador] + " | Enemigo: " + jugadas[rival]);
            if (jugador != rival) {
                return (jugador == 1 && rival == 3)
                        || (jugador == 2 && rival == 1)
                        || (jugador == 3 && rival == 2);
            }
            System.out.println("Empate. Se repite el duelo.");
        }
    }

    // Causa uno o dos puntos de daño y puede envenenar al jugador si sobrevive.
    static void activarTrampaAvanzada() {
        recibirDano(1 + AZAR.nextInt(2), "La trampa avanzada");
        if (vidas > 0 && AZAR.nextBoolean()) {
            veneno = 2;
            System.out.println("Estás envenenado durante los próximos dos turnos.");
        }
    }

    // Sortea uno de los cinco resultados posibles del cofre.
    static void abrirCofre() {
        System.out.println("Abres un cofre.");
        switch (AZAR.nextInt(5)) {
            case 0: ganarOro(4 + AZAR.nextInt(4)); break;
            case 1: curar(2); break;
            case 2:
                pociones++;
                System.out.println("Has encontrado una poción.");
                break;
            case 3: recibirDano(1, "La trampa del cofre"); break;
            case 4: System.out.println("El cofre está vacío."); break;
        }
    }

    // Comprueba el oro antes de cobrar y entregar el objeto o la pista elegida.
    static void comerciar(Scanner entrada) {
        while (true) {
            System.out.println("=== COMERCIANTE === Oro: " + oro);
            System.out.println("1. Poción (3 oro)\n2. Escudo de 4 movimientos (4 oro)\n3. Pista (2 oro)\n4. Volver");
            int opcion = leerOpcion(entrada, 1, 4);
            if (opcion == 4) {
                return;
            }
            int precio = opcion == 1 ? 3 : opcion == 2 ? 4 : 2;
            if (oro < precio) {
                System.out.println("No tienes suficiente oro.");
                continue;
            }
            oro -= precio;
            if (opcion == 1) {
                pociones++;
                System.out.println("Poción comprada.");
            } else if (opcion == 2) {
                escudo += 4;
                System.out.println("El escudo se ha reforzado.");
            } else {
                mostrarPista();
            }
        }
    }

    // Busca primero la llave y, cuando ya está en el inventario, busca el tesoro.
    static void mostrarPista() {
        char objetivo = tieneLlave ? TESORO : LLAVE;
        String nombre = tieneLlave ? "El Tesoro Supremo" : "La Llave Mística";
        for (int f = 0; f < tamano; f++) {
            for (int c = 0; c < tamano; c++) {
                if (tablero[f][c] == objetivo) {
                    System.out.printf("%s está en la fila %d, columna %d.%n", nombre, f + 1, c + 1);
                    return;
                }
            }
        }
    }

    // Elige una pregunta al azar y compara la opción con la respuesta correspondiente.
    static void resolverAcertijo(Scanner entrada) {
        int indice = AZAR.nextInt(PREGUNTAS.length);
        System.out.println(PREGUNTAS[indice]);
        if (leerOpcion(entrada, 1, 3) == RESPUESTAS[indice]) {
            System.out.println("¡Correcto!");
            ganarOro(5);
        } else {
            recibirDano(1, "El acertijo fallado");
        }
    }

    // Limita el daño a las vidas disponibles para que el total no sea negativo.
    static void recibirDano(int dano, String origen) {
        int perdida = Math.min(vidas, dano);
        vidas -= perdida;
        System.out.printf("%s te quita %d vida(s).%n", origen, perdida);
    }

    // Limita la curación al máximo de vidas definido en Configuración.
    static void curar(int cantidad) {
        int recuperadas = Math.min(cantidad, vidasIniciales - vidas);
        vidas += recuperadas;
        System.out.println("Vidas recuperadas: " + recuperadas);
    }

    static void ganarOro(int cantidad) {
        oro += cantidad;
        System.out.println("Has ganado " + cantidad + " de oro.");
    }
}
