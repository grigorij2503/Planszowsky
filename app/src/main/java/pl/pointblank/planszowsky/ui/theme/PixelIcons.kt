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
    'L' to Color.White,      // Light (Błysk)
    'X' to Color.Black,
    'E' to Color(0xFF5D5D5D) // Edges / Inactive grey
)

/**
 * Rysuje ikonę na podstawie listy stringów.
 */
@Composable
fun PixelArtIcon(
    pixelMap: List<String>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val rows = pixelMap.size
        val cols = pixelMap.maxOf { it.length }
        val pixelW = size.width / cols
        val pixelH = size.height / rows

        pixelMap.forEachIndexed { rowIndex, rowString ->
            rowString.forEachIndexed { colIndex, char ->
                val color = pixelColorMap[char]
                if (color != null && color != Color.Transparent) {
                    drawRect(
                        color = color,
                        topLeft = Offset(colIndex * pixelW, rowIndex * pixelH),
                        size = Size(pixelW + 0.5f, pixelH + 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun PixelStar24(isSelected: Boolean) {
    val map = if (isSelected) {
        listOf(
            "........................",
            "...........##...........",
            "...........##...........",
            "..........#YY#..........",
            "..........#YY#..........",
            ".........#YYYY#.........",
            "........#YYYYYY#........",
            "#########YYYYYY#########",
            "#########YYYYYY#########",
            ".#YYYYYYYYYYYYYYYYYYYY#.",
            "..#YYYYYYYYYYYYYYYYYY#..",
            "...#YYYYYYYYYYYYYYYY#...",
            "....#YYYYYYYYYYYYYY#....",
            ".....#YYYYYYYYYYYY#.....",
            ".....#YYYYY##YYYYY#.....",
            "....#YYYYY#..#YYYYY#....",
            "....#YYY#......#YYY#...",
            "....#YYY#......#YYY#....",
            "....#YY#........#YY#....",
            "....#Y#..........#Y#....",
            "........................",
            "........................",
            "........................"
        )
    } else {
        listOf(
            "........................",
            "...........##...........",
            "...........##...........",
            "..........#EE#..........",
            "..........#EE#..........",
            ".........#EEEE#.........",
            "........#EEEEEE#........",
            "#########EEEEEE#########",
            "#########EEEEEE#########",
            ".#EEEEEEEEEEEEEEEEEEEE#.",
            "..#EEEEEEEEEEEEEEEEEE#..",
            "...#EEEEEEEEEEEEEEEE#...",
            "....#EEEEEEEEEEEEEE#....",
            ".....#EEEEEEEEEEEE#.....",
            ".....#EEEEE##EEEEE#.....",
            "....#EEEEE#..#EEEEE#....",
            "....#EEE#......#EEE#...",
            "....#EEE#......#EEE#....",
            "....#EE#........#EE#....",
            "....#E#..........#E#....",
            "........................",
            "........................",
            "........................"
        )
    }
    val starMap = map.map { it.replace('#', if(isSelected) 'Y' else 'X').replace('E', if(isSelected) 'Y' else 'E') }
    PixelArtIcon(starMap, Modifier.fillMaxSize())
}

@Composable
fun PixelChat24(color: Color = RetroBlack) {
    val map = listOf(
        "........................",
        "....################....",
        "...#LLLLLLLLLLLLLLLL#...",
        "..#LLLLLLLLLLLLLLLLLL#..",
        "..#LLLLLLLLLLLLLLLLLL#..",
        "..#LLLLLLLLLLLLLLLLLL#..",
        "..#LLLLLLLLLLLLLLLLLL#..",
        "..#LLLLLLLLLLLLLLLLLL#..",
        "..#LLLLLLLLLLLLLLLLLL#..",
        "..#LLLLLLLLLLLLLLLLLL#..",
        "..#LLLLLLLLLLLLLLLLLL#..",
        "...#LLLLLLLLLLLLLLLL#...",
        "....################....",
        ".........###............",
        "........#LL#............",
        ".......#LL#.............",
        "......#LL#..............",
        ".....#LL#...............",
        "....#LL#................",
        "....####................",
        "........................"
    )
    
    // Custom drawing for chat bubble to ensure white fill
    Canvas(modifier = Modifier.fillMaxSize()) {
        val rows = map.size
        val cols = map[0].length
        val pW = size.width / cols
        val pH = size.height / rows
        map.forEachIndexed { r, row ->
            row.forEachIndexed { c, char ->
                val drawColor = when(char) {
                    '#' -> color
                    'L' -> Color.White
                    else -> Color.Transparent
                }
                if(drawColor != Color.Transparent) {
                    drawRect(drawColor, Offset(c * pW, r * pH), Size(pW + 0.5f, pH + 0.5f))
                }
            }
        }
    }
}

@Composable
fun PixelDelete24(color: Color = RetroBlack) {
    val map = listOf(
        "........................",
        ".........######.........",
        "........#......#........",
        "      ##########        ",
        "     ############       ",
        "     #          #       ",
        "     ############       ",
        "      #        #        ",
        "      # X X X  #        ",
        "      # X X X  #        ",
        "      # X X X  #        ",
        "      # X X X  #        ",
        "      # X X X  #        ",
        "      # X X X  #        ",
        "      # X X X  #        ",
        "      # X X X  #        ",
        "      # X X X  #        ",
        "      #        #        ",
        "      ##########        ",
        "........................"
    ).map { it.replace(' ', '.').replace('#', 'X') }
    
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

@Composable
fun PixelShinyHeart24(isSelected: Boolean) {
    val map = if (isSelected) {
        listOf(
            "........................",
            "......####....####......",
            "....##RRRR#..#RRRR##....",
            "...#RRRRRRR##RRRRRRR#...",
            "..#RRRRRRRRRRRRRRRRRR#..",
            "..#LRRRRRRRRRRRRRRRRR#..",
            ".#LLRRRRRRRRRRRRRRRRRR#.",
            ".#LRRRRRRRRRRRRRRRRRRR#.",
            ".#RRRRRRRRRRRRRRRRRRRR#.",
            ".#RRRRRRRRRRRRRRRRRRRR#.",
            "..#RRRRRRRRRRRRRRRRRR#..",
            "..#RRRRRRRRRRRRRRRRRR#..",
            "...#RRRRRRRRRRRRRRRR#...",
            "....#RRRRRRRRRRRRRR#....",
            ".....#RRRRRRRRRRRR#.....",
            "......#RRRRRRRRRR#......",
            ".......#RRRRRRRR#.......",
            "........#RRRRRR#........",
            ".........#RRRR#.........",
            "..........#RR#..........",
            "...........##...........",
            "........................"
        )
    } else {
        listOf(
            "........................",
            "......####....####......",
            "....##EEEE#..#EEEE##....",
            "...#EEEEEEE##EEEEEEE#...",
            "..#EEEEEEEEEEEEEEEEEE#..",
            "..#LEEEEEEEEEEEEEEEEE#..",
            ".#LLEEEEEEEEEEEEEEEEEE#.",
            ".#LEEEEEEEEEEEEEEEEEEE#.",
            ".#EEEEEEEEEEEEEEEEEEEE#.",
            ".#EEEEEEEEEEEEEEEEEEEE#.",
            "..#EEEEEEEEEEEEEEEEEE#..",
            "..#EEEEEEEEEEEEEEEEEE#..",
            "...#EEEEEEEEEEEEEEEE#...",
            "....#EEEEEEEEEEEEEE#....",
            ".....#EEEEEEEEEEEE#.....",
            "......#EEEEEEEEEE#......",
            ".......#EEEEEEEE#.......",
            "........#EEEEEE#........",
            ".........#EEEE#.........",
            "..........#EE#..........",
            "...........##...........",
            "........................"
        )
    }
    PixelArtIcon(map, Modifier.fillMaxSize())
}

@Composable
fun PixelShinyHeartIcon(isSelected: Boolean) {
    val map = if (isSelected) {
        listOf(
            "............",
            "..##...##...",
            ".#RR#.#RR#..",
            ".#LRR#RRRR#.", 
            ".#RRRRRRRR#.",
            "..#RRRRRR#..",
            "...#RRRR#...",
            "....#RR#....",
            ".....##.....",
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
fun PixelCollectionIcon(isSelected: Boolean) {
    val map = listOf(
        "............",
        "..########..",
        ".#RR#GG#BB#.",
        ".#RR#GG#BB#.",
        ".#RR#GG#BB#.",
        ".#RR#GG#BB#.",
        ".##########.",
        ".#WWWWWWWW#.",
        ".#WWWWWWWW#.",
        ".##########.",
        ".#........#.",
        "............"
    )
    PixelArtIcon(map, Modifier.fillMaxSize())
}

@Composable
fun PixelDiceIcon(isSelected: Boolean) {
    val map = listOf(
        "...............",
        "...............",
        "..###########..",
        "..#OOOOOOOOO#..",
        "..#O#OOOOO#O#..",
        "..#OOOOOOOOO#..",
        "..#OOOOOOOOO#..",
        "..#OOOO#OOOO#..",
        "..#OOOOOOOOO#..",
        "..#OOOOOOOOO#..",
        "..#O#OOOOO#O#..",
        "..#OOOOOOOOO#..",
        "..###########..",
        "...............",
        "..............."
    )
    PixelArtIcon(map, Modifier.fillMaxSize())
}

@Composable
fun PixelProfileIcon(isSelected: Boolean) {
    val map = listOf(
        "............",
        "...######...",
        "..#WWWWWW#..",
        "..#WFFFFW#..",
        "..#F#FF#F#..",
        "..#FFFFFF#..",
        "...######...",
        "..#YYYYYY#..",
        ".#YYYYYYYY#.",
        ".#YYYYYYYY#.",
        ".##########.",
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
        val pixelSize = if (forceSquare) minOf(size.width / cols, size.height / rows) else 0f
        val pixelW = if (forceSquare) pixelSize else size.width / cols
        val pixelH = if (forceSquare) pixelSize else size.height / rows
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
    Canvas(modifier = Modifier.fillMaxSize()) {
        val rows = map.size
        val cols = map.maxOf { it.length }
        val pixelW = size.width / cols
        val pixelH = size.height / rows
        map.forEachIndexed { rowIndex, rowString ->
            rowString.forEachIndexed { colIndex, char ->
                if (char == 'X') {
                    drawRect(color, Offset(colIndex * pixelW, rowIndex * pixelH), Size(pixelW + 0.5f, pixelH + 0.5f))
                } else if (char == '.' && rowIndex < 5 && colIndex > 0 && colIndex < 11) {
                    drawRect(Color.White, Offset(colIndex * pixelW, rowIndex * pixelH), Size(pixelW + 0.5f, pixelH + 0.5f))
                }
            }
        }
    }
}

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

@Composable
fun PixelDeleteIcon(color: Color = RetroBlack) {
    val map = listOf(
        "...XXXXXX...",
        ".XXXXXXXXXX.",
        "X..X....X..X",
        "...XXXXXX...",
        "...X....X...",
        "...X....X...",
        "...X....X...",
        "...X....X...",
        "...XXXXXX..."
    )
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

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

@Composable
fun PixelAddIcon(color: Color = RetroBlack) {
    val map = listOf(
        "............",
        "............",
        ".....XX.....",
        ".....XX.....",
        ".XXXXXXXXXX.",
        ".XXXXXXXXXX.",
        ".....XX.....",
        ".....XX.....",
        "............",
        "............"
    )
    DrawSingleColorPixelIcon(map, color, Modifier.fillMaxSize())
}

@Composable
fun PixelHeart24(color: Color = RetroBlack) {
    val map = listOf(
        "........................",
        "........................",
        "........................",
        "........................",
        ".....######..######.....",
        "....#XXXXXX##XXXXXX#....",
        "...#XXXXXXX#XXXXXXXX#...",
        "...#XXXXXXXXXXXXXXXX#...",
        "...#XXXXXXXXXXXXXXXX#...",
        "...#XXXXXXXXXXXXXXXX#...",
        "...#RRRRRRRRRRRRRRRR#...",
        "....#RRRRRRRRRRRRRR#....",
        ".....#RRRRRRRRRRRR#.....",
        "......#RRRRRRRRRR#......",
        ".......#RRRRRRRR#.......",
        "........#RRRRRR#........",
        ".........#RRRR#.........",
        "..........#RR#..........",
        "...........##...........",
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
