package me.despical.kotl.handlers.setup.components;

import com.github.despical.inventoryframework.pane.StaticPane;

import me.despical.kotl.handlers.setup.SetupInventory;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public interface SetupComponent {

	void prepare(SetupInventory setupInventory);

	void injectComponents(StaticPane pane);
}