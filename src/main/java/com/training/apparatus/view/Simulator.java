package com.training.apparatus.view;

import com.training.apparatus.view.components.TypingBlock;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.security.PermitAll;

@PermitAll
@Route(value = "", layout = MainLayout.class)
@PageTitle("Training apparatus")
public class Simulator extends VerticalLayout {

    private final String text = "Миллионы людей совершали друг против друга такое бесчисленное количество злодеяний, обманов, измен, воровства, подделок и выпуска фальшивых ассигнаций, грабежей, поджогов и убийств, которого в целые века не соберет летопись всех судов мира и на которые, в этот период времени, люди, совершавшие их, не смотрели как на преступления.";

    public Simulator() {
        setSizeFull();
        TypingBlock typingBlock = new TypingBlock();
        typingBlock.setText(text);
        add(typingBlock);
    }

}
