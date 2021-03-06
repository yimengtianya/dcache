package org.dcache.xrootd.door;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dmg.cells.nucleus.CDC;
import dmg.cells.nucleus.CellCommandListener;
import dmg.cells.nucleus.CellInfo;
import dmg.cells.nucleus.CellInfoProvider;

import org.dcache.util.Args;

/**
 * Channel handler that keeps track of connected channels. Provides
 * administrative commands for listing and killing connections.
 */
@Sharable
public class ConnectionTracker
    extends ChannelInboundHandlerAdapter
    implements CellCommandListener, CellInfoProvider
{
    private Map<Channel,String> sessions = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger();

    @Override
    public void channelActive(ChannelHandlerContext ctx)
        throws Exception
    {
        Channel channel = ctx.channel();
        sessions.put(channel, CDC.getSession());
        counter.getAndIncrement();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)
        throws Exception
    {
        try {
            super.channelInactive(ctx);
        } finally {
            sessions.remove(ctx.channel());
        }
    }

    @Override
    public CellInfo getCellInfo(CellInfo info)
    {
        return info;
    }

    @Override
    public void getInfo(PrintWriter pw)
    {
        pw.println(String.format("Active : %d", sessions.size()));
        pw.println(String.format("Created: %d", counter.get()));
    }

    public String ac_connections(Args args)
    {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<Channel, String> e: sessions.entrySet()) {
            s.append(e.getValue()).append(' ').append(e.getKey()).append("\n");
        }
        return s.toString();
    }

    public String ac_kill_$_1(Args args)
    {
        String session = args.argv(0);
        Iterator<String> iterator = sessions.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(session)) {
                iterator.remove();
                return "";
            }
        }
        return "No such connection";
    }
}
