package npo.kib.odc_demo.core.design_system.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import npo.kib.odc_demo.core.design_system.R
import npo.kib.odc_demo.core.design_system.ui.ThemePreviews
import npo.kib.odc_demo.core.design_system.ui.theme.ODCAppTheme


//todo fix scaling and placement of images
@Composable
fun ResponsiveImage(
    modifier: Modifier = Modifier,
    painter: Painter = painterResource(id = R.drawable.profile_pic_sample_square),
    relativeSize: Float = 0.8f,
    contentDescription: String? = "Responsive Image",
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    shape: Shape = CircleShape
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
//                    .requiredHeightIn(100.dp)
    ) {
        Layout(content = {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier,
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter
            )
        },
            measurePolicy = { measurables, constraints ->
                val placeable = measurables.first().measure(constraints)
                // Scale the image so that it's height is 80% of parent
                val height = (constraints.maxHeight * relativeSize).toInt()
//            val width = (height * (placeable.width.toFloat() / placeable.height.toFloat())).toInt()
                val width = (placeable.width.toFloat() * relativeSize).toInt()
//                val middleVerticalLine = createGuideLineFromStart()
                // Return the new layout size
                layout(
                    width,
                    height
                ) {
                    //placing in the center
                    placeable.placeRelative(
                        ((relativeSize - 1) * constraints.maxWidth / 2).toInt(),
                        ((relativeSize - 1) * constraints.maxHeight / 2).toInt(),
                    )
                }
            })
    }
}

@ThemePreviews
@Composable
private fun ResponsiveImagePreview() {
    ODCAppTheme {
        Box(modifier = Modifier.requiredSize(400.dp, 400.dp)) {
        val shape = RoundedCornerShape(50)
        ResponsiveImage(modifier = Modifier.clip(shape))
        }
    }
}