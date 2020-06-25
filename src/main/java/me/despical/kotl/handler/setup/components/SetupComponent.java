package me.despical.kotl.handler.setup.components;

import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import me.despical.kotl.handler.setup.SetupInventory;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public interface SetupComponent {

	void prepare(SetupInventory setupInventory);

	void injectComponents(StaticPane pane);

}