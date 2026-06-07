package thisissadeghi.kickoff.designsystem

import kickoff26.core.designsystem.generated.resources.Res
import kickoff26.core.designsystem.generated.resources.ds_image_placeholder
import kickoff26.core.designsystem.generated.resources.retry_label

/**
 * Created by Ali Sadeghi
 * on 28,Apr,2025
 */
object DesignSystemResources {
    object drawable {
        // Generic loading/error fallback for remote images rendered via AsyncImage.
        val ds_image_placeholder = Res.drawable.ds_image_placeholder
    }

    object string {
        val retry_label = Res.string.retry_label
    }
}
