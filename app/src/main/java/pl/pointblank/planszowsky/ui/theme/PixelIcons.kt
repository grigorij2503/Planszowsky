package pl.pointblank.planszowsky.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Definicja mapowania znaków na kolory
val pixelColorMap = mapOf(
    '#' to RetroBlack,       // Obrys
    '.' to Color.Transparent, // Przezroczystość
    'R' to RetroRed,
    'G' to RetroGreen,
    'B' to RetroBlue,
    'O' to RetroOrange,      // Pomarańcz dla kostki
    'Y' to RetroGold,        // Złoto/Żółty
    'W' to RetroBrown,       // Drewno/Półka
    'S' to Color(0xFFE0E0E0),// Srebrny/Szary (Hełm)
    'F' to Color(0xFFF8B888),// Twarz (Face)
    'L' to Color.White       // Light (Błysk)
)

/**
 * Rysuje ikonę na podstawie listy stringów.
 * Każdy znak w stringu to jeden "duży piksel".
 */
@Composable
fun PixelArtIcon(
    pixelMap: List<String>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val rows = pixelMap.size
        val cols = pixelMap.maxOf { it.length }

        // Automatycznie obliczamy wielkość piksela, żeby wypełnić dostępny obszar
        val pixelW = size.width / cols
        val pixelH = size.height / rows

        pixelMap.forEachIndexed { rowIndex, rowString ->
            rowString.forEachIndexed { colIndex, char ->
                val color = pixelColorMap[char]
                if (color != null && color != Color.Transparent) {
                    drawRect(
                        color = color,
                        topLeft = Offset(colIndex * pixelW, rowIndex * pixelH),
                        // Dodajemy minimalny nadmiar (+0.5f), żeby usunąć białe linie między pikselami na niektórych ekranach
                        size = Size(pixelW + 0.5f, pixelH + 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun PixelCollectionIcon(isSelected: Boolean) {
    // Półka z książkami - wersja detailed
    // # - obrys, W - drewno, kolory książek
    val map = listOf(
        "............",
        "..########..",
        ".#RR#GG#BB#.",
        ".#RR#GG#BB#.",
        ".#RR#GG#BB#.",
        ".#RR#GG#BB#.",
        ".##########.",
        ".#WWWWWWWW#.",  // Półka
        ".#WWWWWWWW#.",
        ".##########.",
        ".#........#.",  // Cień pod półką
        "............"
    )
    PixelArtIcon(map, Modifier.fillMaxSize())
}

@Composable
fun PixelDiceIcon(isSelected: Boolean) {
    // Izometryczna kostka
    // O - Pomarańcz, # - Oczka i obrys
    val map = listOf(
        "...............", // Margines góra
        "...............",
        "..###########..", // Górna krawędź
        "..#OOOOOOOOO#..",
        "..#O#OOOOO#O#..", // Oczka (Góra-Lewo, Góra-Prawo)
        "..#OOOOOOOOO#..",
        "..#OOOOOOOOO#..",
        "..#OOOO#OOOO#..", // Oczko (Środek)
        "..#OOOOOOOOO#..",
        "..#OOOOOOOOO#..",
        "..#O#OOOOO#O#..", // Oczka (Dół-Lewo, Dół-Prawo)
        "..#OOOOOOOOO#..",
        "..###########..", // Dolna krawędź
        "...............", // Margines dół
        "..............."
    )
    PixelArtIcon(map, Modifier.fillMaxSize())
}

@Composable
fun PixelStarIcon(isSelected: Boolean, color: Color = RetroBlack) {
    val map = if (isSelected) {
        listOf(
            ".....X.....",
            "....XXX....",
            "....XXX....",
            "XXXXXXXXXXX",
            "..XXXXXXX..",
            "...XXXXX...",
            "..XXXXXXX..",
            ".XXX...XXX.",
            "XX.......XX"
        )
    } else {
        listOf(
            ".....X.....",
            "....X.X....",
            "....X.X....",
            ".XXX...XXX.",
            "..X.....X..",
            "...X...X...",
            "..X.....X..",
            ".X.......X.",
            "X.........X"
        )
    }
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

@Composable
fun PixelHeartIcon(isSelected: Boolean, color: Color = RetroBlack) {
    val map = if (isSelected) {
        listOf(
            "..XX...XX..",
            ".XXXX.XXXX.",
            "XXXXXXXXXXX",
            "XXXXXXXXXXX",
            "XXXXXXXXXXX",
            ".XXXXXXXXX.",
            "..XXXXXXX..",
            "...XXXXX...",
            "....XXX....",
            ".....X....."
        )
    } else {
        listOf(
            "..XX...XX..",
            ".X..X.X..X.",
            "X....X....X",
            "X.........X",
            "X.........X",
            ".X.......X.",
            "..X.....X..",
            "...X...X...",
            "....X.X....",
            ".....X....."
        )
    }
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

@Composable
fun PixelShinyHeartIcon(isSelected: Boolean) {
    // Serce z błyskiem (L) - Retro RPG style
    val map = if (isSelected) {
        listOf(
            "............",
            "..##...##...",
            ".##R#.#R##..", // L = Biały Błysk
            ".#RLR#RRR#..", // # = Ciemny obrys
            ".#RRRRRRR#..", // R = Czerwone wypełnienie
            "..#RRRRR#..",
            "...#RRR#...",
            "....#R#....",
            ".....#.....",
            "............"
        )
    } else {
        listOf(
            "............",
            "..##...##...",
            ".#..#.#..#..",
            ".#L..#....#.", 
            ".#........#.",
            "..#......#..",
            "...#....#...",
            "....#..#....",
            ".....##.....",
            "............"
        )
    }
    PixelArtIcon(map, Modifier.fillMaxSize())
}

@Composable
fun PixelHeart24(color: Color = RetroBlack) {
    val map = listOf(
        "........................",
        "........................",
        "........................",
        "........................",
        ".....######..######.....",
        "....#RRRRRR##RRRRRR#....",
        "...#LLRRRRRRRRRRRRRR#...", // L = Błysk światła
        "...#LLRRRRRRRRRRRRRR#...",
        "...#LRRRRRRRRRRRRRRR#...",
        "...#RRRRRRRRRRRRRRRR#...",
        "...#RRRRRRRRRRRRRRRR#...",
        "....#RRRRRRRRRRRRRR#....",
        ".....#RRRRRRRRRRRR#.....",
        "......#RRRRRRRRRR#......",
        ".......#RRRRRRRR#.......",
        "........#RRRRRR#........",
        ".........#RRRR#.........",
        "..........#RR#..........",
        "...........##...........", // Idealny środek dla parzystej szerokości
        "........................",
        "........................",
        "........................",
        "........................",
        "........................"
    )
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

@Composable
fun PixelPlus24(color: Color = RetroBlack) {
    val map = listOf(
        "........................",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        "..XXXXXXXXXXXXXXXXXXXX..",
        "..XXXXXXXXXXXXXXXXXXXX..",
        "..XXXXXXXXXXXXXXXXXXXX..",
        "..XXXXXXXXXXXXXXXXXXXX..",
        "..XXXXXXXXXXXXXXXXXXXX..",
        "..XXXXXXXXXXXXXXXXXXXX..",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        ".........XXXXXX.........",
        "........................"
    )
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

@Composable
fun PixelProfileIcon(isSelected: Boolean) {
    // Rycerz w hełmie (S - stal, # - wizjer)
    val map = listOf(
        "............",
        "...######...", // Góra włosów
        "..#WWWWWW#..", // Włosy
        "..#WFFFFW#..", // Włosy po bokach + czoło
        "..#F#FF#F#..", // Oczy (# to czarny piksel na skórze)
        "..#FFFFFF#..", // Policzki
        "...######...", // Broda / Szyja
        "..#YYYYYY#..", // Ramiona (Żółta koszulka)
        ".#YYYYYYYY#.", // Tłów
        ".#YYYYYYYY#.",
        ".##########.", // Dół
        "............"
    )
    PixelArtIcon(map, Modifier.fillMaxSize())
}


@Composable
private fun DrawSingleColorPixelIcon(
    map: List<String>,
    color: Color,
    modifier: Modifier,
    forceSquare: Boolean = true
) {
    Canvas(modifier = modifier) {
        val rows = map.size
        val cols = map.maxOf { it.length }
        
        val pixelSize = if (forceSquare) {
            minOf(size.width / cols, size.height / rows)
        } else {
            0f
        }
        
        val pixelW = if (forceSquare) pixelSize else size.width / cols
        val pixelH = if (forceSquare) pixelSize else size.height / rows
        
        // Centrowanie ikony jeśli wymusiliśmy kwadratowe piksele
        val offsetX = (size.width - (cols * pixelW)) / 2
        val offsetY = (size.height - (rows * pixelH)) / 2

        map.forEachIndexed { rowIndex, rowString ->
            rowString.forEachIndexed { colIndex, char ->
                if (char != '.') {
                    drawRect(
                        color = color,
                        topLeft = Offset(offsetX + colIndex * pixelW, offsetY + rowIndex * pixelH),
                        size = Size(pixelW + 0.1f, pixelH + 0.1f)
                    )
                }
            }
        }
    }
}

// 1. PixelPlusIcon (Gruby plus)
@Composable
fun PixelPlusIcon(color: Color = RetroBlack) {
    val map = listOf(
        "...........",
        "............",
        ".....XX.....",
        ".....XX.....",
        "...XXXXXX...",
        "...XXXXXX...",
        ".....XX.....",
        ".....XX.....",
        "............",
        "............"
    )
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

// 2. PixelSearchIcon (Lupa - poprawiona geometria)
@Composable
fun PixelSearchIcon(color: Color = RetroBlack, modifier: Modifier = Modifier.fillMaxSize()) {
    val map = listOf(
        ".XXXXXX.....",
        "X......X....",
        "X......X....",
        "X......X....",
        "X......X....",
        ".XXXXXX.....",
        "......X.....",
        ".......X....",
        "........X...",
        ".........X.."
    )
    DrawSingleColorPixelIcon(map, color, modifier)
}

// 3. PixelChatIcon (Dymek rozmowy)
@Composable
fun PixelChatIcon(color: Color = RetroBlack) {
    val map = listOf(
        "............",
        ".XXXXXXXXXX.",
        ".X........X.",
        ".X........X.",
        ".X........X.",
        ".XXXXXXXXXX.",
        "..XX........",
        ".XX.........",
        "XX.........."
    )
    // Rysujemy wypełniony dymek
    Canvas(modifier = Modifier.fillMaxSize()) {
        val rows = map.size
        val cols = map.maxOf { it.length }
        val pixelW = size.width / cols
        val pixelH = size.height / rows

        map.forEachIndexed { rowIndex, rowString ->
            rowString.forEachIndexed { colIndex, char ->
                if (char == 'X') {
                    // Obrys
                    drawRect(color, Offset(colIndex * pixelW, rowIndex * pixelH), Size(pixelW + 0.5f, pixelH + 0.5f))
                } else if (char == '.' && rowIndex < 5 && colIndex > 0 && colIndex < 11) {
                    // Wnętrze dymku (możesz zmienić kolor na biały/kremowy jeśli wolisz)
                    drawRect(Color.White, Offset(colIndex * pixelW, rowIndex * pixelH), Size(pixelW + 0.5f, pixelH + 0.5f))
                }
            }
        }
    }
}

// 4. PixelBackIcon (Strzałka w lewo)
@Composable
fun PixelBackIcon(color: Color = RetroBlack) {
    val map = listOf(
        "............",
        "....XX......",
        "...XXX......",
        "..XXXX......",
        ".XXXXXXXXXX.",
        "..XXXX......",
        "...XXX......",
        "....XX......",
        "............"
    )
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

// 5. PixelDeleteIcon (Kosz na śmieci)
@Composable
fun PixelDeleteIcon(color: Color = RetroBlack) {
    val map = listOf(
        "...XXXXXX...",
        ".XXXXXXXXXX.",
        "X..X....X..X", // Pokrywa uniesiona (detal)
        "...XXXXXX...",
        "...X....X...",
        "...X....X...",
        "...X....X...",
        "...X....X...",
        "...XXXXXX..."
    )
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

// 6. PixelSendIcon (Papierowy samolot / Kursor)
@Composable
fun PixelSendIcon(color: Color = RetroBlack) {
    val map = listOf(
        "............",
        ".X..........",
        ".XXX........",
        ".XXXX.......",
        ".XXXXXXXX..",
        ".XXXX.......",
        ".XXX........",
        ".X..........",
        "............"
    )
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

