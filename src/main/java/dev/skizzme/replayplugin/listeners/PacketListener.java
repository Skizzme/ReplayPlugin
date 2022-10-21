package dev.skizzme.replayplugin.listeners;

import dev.skizzme.replayplugin.handlers.PacketHandler;
import io.github.retrooper.packetevents.event.PacketListenerDynamic;
import io.github.retrooper.packetevents.event.impl.PacketPlayReceiveEvent;
import io.github.retrooper.packetevents.event.impl.PacketPlaySendEvent;
import io.github.retrooper.packetevents.event.priority.PacketEventPriority;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PacketListener extends PacketListenerDynamic {

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	public PacketListener() {
		super(PacketEventPriority.MONITOR);
	}

	@Override
	public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
		executor.execute(() -> PacketHandler.processIncoming(event));
		super.onPacketPlayReceive(event);
	}

	@Override
	public void onPacketPlaySend(PacketPlaySendEvent event) {
		executor.execute(() -> PacketHandler.processOutgoing(event));
		super.onPacketPlaySend(event);
	}
}