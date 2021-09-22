package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentContent {
    private Integer id;
    private byte[] bytes;
    private Attachment attachment;
}
